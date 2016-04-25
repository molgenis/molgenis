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
			rebuildIndex();
		});
	}

	private void rebuildIndex()
	{
		LOG.info("## Start rebuilding index: [" + new Date() + "]");
		
		Stream<Entity> logEntries = getAllLogEntries(this.transactionId);

		logEntries.forEach(e -> {
			requireNonNull(e.getEntityMetaData());
			if (e.getString(IndexTransactionLogEntryMetaData.ENTITY_ID) != null)
			{
				Entity entity = dataService.findOneById(e.getString(IndexTransactionLogEntryMetaData.ENTITY_FULL_NAME),
						e.getString(IndexTransactionLogEntryMetaData.ENTITY_ID));

				// TODO CHECK java.lang.NullPointerException give the right message when this happens
				// Check if transaction is allready finished.

				switch (CudType.valueOf(e.getString(IndexTransactionLogEntryMetaData.CUD_TYPE)))
				{
						case ADD:
						this.searchService.index(entity, entity.getEntityMetaData(), IndexingMode.ADD);
						break;
						case DELETE:
						this.searchService.delete(entity, entity.getEntityMetaData());
						break;
						case UPDATE:
						this.searchService.index(entity, entity.getEntityMetaData(), IndexingMode.UPDATE);
						break;
					default:
						break;
				}
			}
			else if (e.getString(IndexTransactionLogEntryMetaData.DATA_TYPE).equals(DataType.DATA.name()))
			{
				String entityFullName = e.getString(IndexTransactionLogEntryMetaData.ENTITY_FULL_NAME);
				EntityMetaData entityMetaData = dataService.getMeta().getEntityMetaData(entityFullName);
				Stream<Entity> entities = dataService.findAll(entityFullName);
				this.searchService.delete(entityFullName);
				this.searchService.createMappings(entityMetaData);
				this.searchService.index(entities,entityMetaData, IndexingMode.ADD);
			}
			else
			{
				String entityFullName = e.getString(IndexTransactionLogEntryMetaData.ENTITY_FULL_NAME);
				EntityMetaData entityMetaData = dataService.getMeta().getEntityMetaData(entityFullName);
				Stream<Entity> entities = dataService.findAll(entityFullName);
				this.searchService.delete(entityFullName);
				switch (CudType.valueOf(e.getString(IndexTransactionLogEntryMetaData.CUD_TYPE)))
				{
					case UPDATE:
					case ADD:
						this.searchService.createMappings(entityMetaData);
						this.searchService.index(entities, entityMetaData, IndexingMode.ADD);
						break;
					case DELETE:
						break;
					default:
						break;
				}
			}
		});

		this.searchService.refreshIndex();
		LOG.info("## End rebuilding index: [{}]", new Date());
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
