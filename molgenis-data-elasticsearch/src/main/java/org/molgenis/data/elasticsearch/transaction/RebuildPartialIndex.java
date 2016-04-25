package org.molgenis.data.elasticsearch.transaction;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.util.Date;
import java.util.stream.Stream;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.Sort;
import org.molgenis.data.elasticsearch.ElasticsearchService.IndexingMode;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.transaction.log.index.IndexTransactionLogEntryMetaData;
import org.molgenis.data.transaction.log.index.IndexTransactionLogEntryMetaData.CudType;
import org.molgenis.data.transaction.log.index.IndexTransactionLogEntryMetaData.DataType;
import org.molgenis.data.transaction.log.index.IndexTransactionLogMetaData;
import org.molgenis.data.transaction.log.index.IndexTransactionLogMetaData.IndexStatus;
import org.molgenis.data.transaction.log.index.IndexTransactionLogMetaData.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RebuildPartialIndex implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger(RebuildPartialIndex.class);
	private final String transactionId;
	private final DataService dataService;
	private final SearchService searchService;

	public RebuildPartialIndex(String transactionId, DataService dataService, SearchService searchService)
	{
		this.transactionId = requireNonNull(transactionId);
		this.dataService = requireNonNull(dataService);
		this.searchService = requireNonNull(searchService);
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		runAsSystem(() -> {
			LOG.info("--- Start rebuilding index: [" + new Date() + "]");
			Entity transLog = dataService.findOneById(IndexTransactionLogMetaData.ENTITY_NAME, this.transactionId);
			TransactionStatus transactionStatus = TransactionStatus.valueOf(transLog
					.getString(IndexTransactionLogMetaData.TRANSACTION_STATUS));
			IndexStatus indexStatus = IndexStatus.valueOf(transLog.getString(IndexTransactionLogMetaData.INDEX_STATUS));

			if (transactionStatus.equals(TransactionStatus.COMMITED) && indexStatus.equals(IndexStatus.NONE))
			{
				rebuildIndex();
				transLog.set(IndexTransactionLogMetaData.INDEX_STATUS, IndexStatus.FINISHED);
			}
			else
			{
				transLog.set(IndexTransactionLogMetaData.INDEX_STATUS, IndexStatus.CANCELED);
				LOG.error(
						"[Reindex transaction [{}] is canceled] When rebuilding index transaction status must be COMMITED and index status must be NONE. Current values are: TRANSACTION_STATUS [{}] INDEX_STATUS [{}] ",
						transactionId, transactionStatus, indexStatus);
			}

			dataService.update(IndexTransactionLogMetaData.ENTITY_NAME, transLog);
			LOG.info("--- End rebuilding index: [{}]", new Date());
		});
	}

	private void rebuildIndex()
	{
		Stream<Entity> logEntries = getAllLogEntries(this.transactionId);
		logEntries.forEach(e -> {
			requireNonNull(e.getEntityMetaData());
			final CudType cudType = CudType.valueOf(e.getString(IndexTransactionLogEntryMetaData.CUD_TYPE));
			final String entityFullName = e.getString(IndexTransactionLogEntryMetaData.ENTITY_FULL_NAME);
			final EntityMetaData entityMetaData = dataService.getMeta().getEntityMetaData(entityFullName);
			final String entityId = e.getString(IndexTransactionLogEntryMetaData.ENTITY_ID);
			final DataType dataType = DataType.valueOf(e.getString(IndexTransactionLogEntryMetaData.DATA_TYPE));
			
			if (entityId != null)
			{
				this.rebuildIndexOneEntity(this.transactionId, entityFullName, entityId, cudType);
			}
			else if (dataType.equals(DataType.DATA))
			{
				this.rebuildIndexBatchEntities(this.transactionId, entityFullName, entityMetaData, cudType);
			}
			else
			{
				this.rebuildIndexEntityMeta(this.transactionId, entityFullName, entityMetaData, cudType);
			}
		});

		this.searchService.refreshIndex();
	}

	private void rebuildIndexOneEntity(String transactionId, String entityFullName, String entityId, CudType cudType)
	{
		Entity entity = dataService.findOneById(entityFullName, entityId);
		switch (cudType)
		{
			case ADD:
				this.searchService.index(entity, entity.getEntityMetaData(), IndexingMode.ADD);
				break;
			case UPDATE:
				this.searchService.index(entity, entity.getEntityMetaData(), IndexingMode.UPDATE);
				break;
			case DELETE:
				this.searchService.deleteByIdNoValidation(entityId, entityFullName);
				break;
			default:
				break;
		}
	}
	
	private void rebuildIndexBatchEntities(String transactionId, String entityFullName, EntityMetaData entityMetaData,
			CudType cudType)
	{
		Stream<Entity> entities = dataService.findAll(entityFullName);
		switch (cudType)
		{
			case UPDATE:
			case ADD:
				this.searchService.deleteEntitiesNoValidation(entities, entityMetaData);
				entities = dataService.findAll(entityFullName);
				this.searchService.index(entities, entityMetaData, IndexingMode.ADD);
				break;
			case DELETE:
				this.searchService.deleteEntitiesNoValidation(entities, entityMetaData);
				break;
			default:
				break;
		}
	}

	private void rebuildIndexEntityMeta(String transactionId, String entityFullName, EntityMetaData entityMetaData,
			CudType cudType)
	{
		switch (cudType)
		{
			case UPDATE:
			case ADD:
				this.searchService.delete(entityFullName);
				this.searchService.createMappings(entityMetaData);
				Stream<Entity> entities = dataService.findAll(entityFullName);
				this.searchService.index(entities, entityMetaData, IndexingMode.ADD);
				break;
			case DELETE:
				this.searchService.delete(entityFullName);
				break;
			default:
				break;
		}
	}

	/**
	 * Get all relevant logs with transaction id. Sort on log order
	 * 
	 * @return
	 */
	private Stream<Entity> getAllLogEntries(String transactionId)
	{
		QueryRule rule = new QueryRule(IndexTransactionLogEntryMetaData.MOLGENIS_TRANSACTION_LOG, Operator.EQUALS,
				transactionId);
		QueryImpl<Entity> q = new QueryImpl<Entity>(rule);
		q.setSort(new Sort(IndexTransactionLogEntryMetaData.LOG_ORDER));
		return dataService.findAll(IndexTransactionLogEntryMetaData.ENTITY_NAME, q);
	}
}
