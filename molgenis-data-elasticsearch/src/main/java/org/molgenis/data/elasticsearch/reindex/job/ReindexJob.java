package org.molgenis.data.elasticsearch.reindex.job;

import static java.text.MessageFormat.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.elasticsearch.reindex.meta.ReindexActionJobMetaData.COUNT;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.Sort;
import org.molgenis.data.elasticsearch.ElasticsearchService.IndexingMode;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexActionJobMetaData;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexActionMetaData;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexActionMetaData.CudType;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexActionMetaData.DataType;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexActionMetaData.ReindexStatus;
import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.support.QueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionTemplate;

public class ReindexJob extends Job
{
	private static final Logger LOG = LoggerFactory.getLogger(ReindexJob.class);
	private final String transactionId;
	private final DataService dataService;
	private final SearchService searchService;

	ReindexJob(Progress progress, Authentication authentication, String transactionId, DataService dataService,
			SearchService searchService)
	{
		super(progress, null, authentication);
		this.transactionId = requireNonNull(transactionId);
		this.dataService = requireNonNull(dataService);
		this.searchService = requireNonNull(searchService);
	}

	@Override
	public Void call(Progress progress)
	{
		Entity entity = this.dataService.findOneById(ReindexActionJobMetaData.ENTITY_NAME, transactionId);
		if (null != entity)
		{
			progress.setProgressMax(entity.getInt(COUNT));
			progress.status(format("######## START Reindex transaction id: [{0}] ########", transactionId));
			rebuildIndex(progress);
			progress.status(format("######## END Reindex transaction id: [{0}] took {1} ms. ########", transactionId,
					progress.timeRunning()));
		}
		else
		{
			progress.status(format("No reindex action job found for transaction id: [{0}]", transactionId));
		}
		return null;
	}

	private void rebuildIndex(Progress progress)
	{
		AtomicInteger count = new AtomicInteger();
		Stream<Entity> logEntries = getAllReindexActions(this.transactionId);
		logEntries.forEach(e -> {
			requireNonNull(e.getEntityMetaData());
			final CudType cudType = CudType.valueOf(e.getString(ReindexActionMetaData.CUD_TYPE));
			final String entityFullName = e.getString(ReindexActionMetaData.ENTITY_FULL_NAME);
			final EntityMetaData entityMetaData = dataService.getMeta().getEntityMetaData(entityFullName);
			final String entityId = e.getString(ReindexActionMetaData.ENTITY_ID);
			final DataType dataType = DataType.valueOf(e.getString(ReindexActionMetaData.DATA_TYPE));

			this.updateActionStatus(e, ReindexStatus.STARTED);
			if (entityId != null)
			{
				progress.progress(count.getAndIncrement(),
						format("Reindexing {0}.{1}. CUDType = {2}", entityFullName, entityId, cudType));
				this.rebuildIndexOneEntity(entityFullName, entityId, cudType);
			}
			else if (dataType.equals(DataType.DATA))
			{
				progress.progress(count.getAndIncrement(),
						format("Reindexing entity {0} in batch. CUDType = {2}", entityFullName, cudType));
				this.rebuildIndexBatchEntities(entityFullName, entityMetaData, cudType);
			}
			else
			{
				progress.progress(count.getAndIncrement(),
						format("Reindexing entity {0} in batch due to metadata change. CUDType = {2}", entityFullName,
								cudType));
				this.rebuildIndexEntityMeta(entityFullName, entityMetaData, cudType);
			}
			this.updateActionStatus(e, ReindexStatus.FINISHED);
		});
		progress.status("refreshIndex...");
		this.searchService.refreshIndex();
		progress.status("refreshIndex done.");
	}

	private void updateActionStatus(Entity e, ReindexStatus status)
	{
		e.set(ReindexActionMetaData.REINDEX_STATUS, status);
		dataService.update(ReindexActionMetaData.ENTITY_NAME, e);
	}

	private void rebuildIndexOneEntity(String entityFullName, String entityId, CudType cudType)
	{
		LOG.info("# Reindex row id [{}] entity: [{}] cud: [{}]", entityId, entityFullName, cudType);
		switch (cudType)
		{
			case ADD:
				Entity entityA = dataService.findOneById(entityFullName, entityId);
				this.searchService.index(entityA, entityA.getEntityMetaData(), IndexingMode.ADD);
				break;
			case UPDATE:
				Entity entityU = dataService.findOneById(entityFullName, entityId);
				this.searchService.index(entityU, entityU.getEntityMetaData(), IndexingMode.UPDATE);
				break;
			case DELETE:
				this.searchService.deleteById(entityId, dataService.getMeta().getEntityMetaData(entityFullName));
				break;
			default:
				break;
		}
	}

	private void rebuildIndexBatchEntities(String entityFullName, EntityMetaData entityMetaData, CudType cudType)
	{
		LOG.info("# Reindex batch entities of entity: [{}] cud: [{}]", entityFullName, cudType);
		this.searchService.rebuildIndex(dataService.getRepository(entityFullName), entityMetaData);
	}

	private void rebuildIndexEntityMeta(String entityFullName, EntityMetaData entityMetaData, CudType cudType)
	{
		LOG.info("# Reindex data and metadata rebuild whole index of entity: [{}] cud: [{}]", entityFullName, cudType);
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
