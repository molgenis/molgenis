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
import org.molgenis.data.elasticsearch.reindex.ReindexActionMetaData;
import org.molgenis.data.elasticsearch.reindex.ReindexActionMetaData.CudType;
import org.molgenis.data.elasticsearch.reindex.ReindexActionMetaData.DataType;
import org.molgenis.data.support.QueryImpl;
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
			LOG.info("--- Start rebuilding index: [{}] group id: [{}]", new Date(), transactionId);
			rebuildIndex();
			LOG.info("--- End rebuilding index: [{}] group id: [{}]", new Date(), transactionId);
		});
	}

	private void rebuildIndex()
	{
		Stream<Entity> logEntries = getAllReindexActions(this.transactionId);
		logEntries.forEach(e -> {
			requireNonNull(e.getEntityMetaData());
			final CudType cudType = CudType.valueOf(e.getString(ReindexActionMetaData.CUD_TYPE));
			final String entityFullName = e.getString(ReindexActionMetaData.ENTITY_FULL_NAME);
			final EntityMetaData entityMetaData = dataService.getMeta().getEntityMetaData(entityFullName);
			final String entityId = e.getString(ReindexActionMetaData.ENTITY_ID);
			final DataType dataType = DataType.valueOf(e.getString(ReindexActionMetaData.DATA_TYPE));

			if (entityId != null)
			{
				this.rebuildIndexOneEntity(entityFullName, entityId, cudType);
			}
			else if (dataType.equals(DataType.DATA))
			{
				this.rebuildIndexBatchEntities(entityFullName, entityMetaData);
			}
			else
			{
				this.rebuildIndexEntityMeta(entityFullName, entityMetaData, cudType);
			}
		});

		this.searchService.refreshIndex();
	}

	private void rebuildIndexOneEntity(String entityFullName, String entityId, CudType cudType)
	{
		switch (cudType)
		{
			case ADD:
				Entity entityA = dataService.findOneById(entityFullName, entityId);
				this.searchService.index(entityA, entityA.getEntityMetaData(), IndexingMode.ADD);
				LOG.info("Reindex row id [{}] entity full name: [{}] cud: [{}]", entityId, entityFullName, CudType.ADD);
				break;
			case UPDATE:
				Entity entityU = dataService.findOneById(entityFullName, entityId);
				this.searchService.index(entityU, entityU.getEntityMetaData(), IndexingMode.UPDATE);
				LOG.info("Reindex row id [{}] entity full name: [{}] cud: [{}]", entityId, entityFullName,
						CudType.UPDATE);
				break;
			case DELETE:
				this.searchService.deleteById(entityId, dataService.getMeta().getEntityMetaData(entityFullName));
				LOG.info("Reindex row id [{}] entity full name: [{}] cud: [{}]", entityId, entityFullName,
						CudType.DELETE);
				break;
			default:
				break;
		}
	}
	
	private void rebuildIndexBatchEntities(String entityFullName, EntityMetaData entityMetaData)
	{
		this.searchService.rebuildIndex(dataService.getRepository(entityFullName), entityMetaData);
	}

	private void rebuildIndexEntityMeta(String entityFullName, EntityMetaData entityMetaData,
			CudType cudType)
	{
		switch (cudType)
		{
			case UPDATE:
			case ADD:
				this.searchService.rebuildIndex(dataService.getRepository(entityFullName), entityMetaData);
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
	private Stream<Entity> getAllReindexActions(String transactionId)
	{
		QueryRule rule = new QueryRule(ReindexActionMetaData.REINDEX_ACTION_GROUP, Operator.EQUALS, transactionId);
		QueryImpl<Entity> q = new QueryImpl<Entity>(rule);
		q.setSort(new Sort(ReindexActionMetaData.ACTION_ORDER));
		return dataService.findAll(ReindexActionMetaData.ENTITY_NAME, q);
	}
}
