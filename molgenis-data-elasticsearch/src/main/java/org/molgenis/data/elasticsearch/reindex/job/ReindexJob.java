package org.molgenis.data.elasticsearch.reindex.job;

import static java.text.MessageFormat.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.QueryRule.Operator.EQUALS;
import static org.molgenis.data.reindex.meta.ReindexActionJobMetaData.COUNT;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.ACTION_ORDER;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.REINDEX_ACTION_GROUP;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType.DELETE;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.DataType.DATA;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.ReindexStatus.FAILED;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.ReindexStatus.FINISHED;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.ReindexStatus.STARTED;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.Sort;
import org.molgenis.data.elasticsearch.ElasticsearchService.IndexingMode;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.reindex.meta.ReindexActionJobMetaData;
import org.molgenis.data.reindex.meta.ReindexActionMetaData;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.DataType;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.ReindexStatus;
import org.molgenis.data.support.QueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

/**
 * {@link Job} that executes a bunch of {@link ReindexActionMetaData} stored in a {@link ReindexActionJobMetaData}.
 */
class ReindexJob extends Job
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
		requireNonNull(progress);
		Entity reindexActionEntity = dataService.findOneById(ReindexActionJobMetaData.ENTITY_NAME, transactionId);
		if (reindexActionEntity != null && reindexActionEntity.getInt(COUNT) != null
				&& reindexActionEntity.getInt(COUNT) > 0)
		{
			progress.setProgressMax(reindexActionEntity.getInt(COUNT));
			progress.status(format("######## START Reindex transaction id: [{0}] ########", transactionId));
			performReindexActions(progress);
			progress.status(format("######## END Reindex transaction id: [{0}] ########", transactionId));
		}
		else
		{
			progress.status(format("No reindex actions found for transaction id: [{0}]", transactionId));
		}
		return null;
	}

	/**
	 * Performs the ReindexActions.
	 *
	 * @param progress
	 *            {@link Progress} instance to log progress information to
	 */
	private void performReindexActions(Progress progress)
	{
		AtomicInteger count = new AtomicInteger();
		Stream<Entity> logEntries = dataService.findAll(ReindexActionMetaData.ENTITY_NAME,
				createQueryGetAllReindexActions(this.transactionId));
		try
		{
			logEntries.forEach(e -> {
				requireNonNull(e.getEntityMetaData());
				final CudType cudType = CudType.valueOf(e.getString(ReindexActionMetaData.CUD_TYPE));
				final String entityFullName = e.getString(ReindexActionMetaData.ENTITY_FULL_NAME);
				final EntityMetaData entityMetaData = dataService.getMeta().getEntityMetaData(entityFullName);
				final String entityId = e.getString(ReindexActionMetaData.ENTITY_ID);
				final DataType dataType = DataType.valueOf(e.getString(ReindexActionMetaData.DATA_TYPE));

				this.updateActionStatus(e, STARTED);
				try
				{
					if (entityId != null)
					{
						progress.progress(count.getAndIncrement(),
								format("Reindexing {0}.{1}, CUDType = {2}", entityFullName, entityId, cudType));
						this.rebuildIndexOneEntity(entityFullName, entityId, cudType);
					}
					else if (dataType.equals(DATA) || cudType != DELETE)
					{
						progress.progress(count.getAndIncrement(),
								format("Reindexing repository {0}. CUDType = {1}", entityFullName, cudType));
						this.rebuildIndexBatchEntities(entityMetaData);
					}
					else
					{
						progress.progress(count.getAndIncrement(),
								format("Dropping index of repository {0}.", entityFullName));
						this.searchService.delete(entityFullName);
					}
					this.updateActionStatus(e, FINISHED);
				}
				catch (Exception ex)
				{
					updateActionStatus(e, FAILED);
					throw ex;
				}
			});
			progress.progress(count.get(), "Executed all reindex actions.");
		}
		finally
		{
			progress.status("refreshIndex...");
			this.searchService.refreshIndex();
			progress.status("refreshIndex done.");
		}
	}

	/**
	 * Updates the {@link ReindexStatus} of a ReindexAction and stores the change.
	 *
	 * @param reindexAction
	 *            the ReindexAction of which the status is updated
	 * @param status
	 *            the new {@link ReindexStatus}
	 */
	private void updateActionStatus(Entity reindexAction, ReindexStatus status)
	{
		reindexAction.set(ReindexActionMetaData.REINDEX_STATUS, status);
		dataService.update(ReindexActionMetaData.ENTITY_NAME, reindexAction);
	}

	/**
	 * Reindexes one single entity instance.
	 *
	 * @param entityFullName
	 *            the fully qualified name of the entity's repository
	 * @param entityId
	 *            the identifier of the entity to update
	 * @param cudType
	 *            the {@link CudType} of the change that was made to the entity
	 */
	private void rebuildIndexOneEntity(String entityFullName, String entityId, CudType cudType)
	{
		LOG.debug("Reindexing [{}].[{}]... cud: [{}]", entityFullName, entityId, cudType);
		switch (cudType)
		{
			case CREATE:
				Entity entityA = dataService.findOneById(entityFullName, entityId);
				searchService.index(entityA, entityA.getEntityMetaData(), IndexingMode.ADD);
				break;
			case UPDATE:
				Entity entityU = dataService.findOneById(entityFullName, entityId);
				searchService.index(entityU, entityU.getEntityMetaData(), IndexingMode.UPDATE);
				break;
			case DELETE:
				// TODO This calls the version that checks for references! But to prevent race conditions the reindexer
				// must delete the document even if references exist
				searchService.deleteById(entityId, dataService.getMeta().getEntityMetaData(entityFullName));
				break;
		}
		LOG.info("Reindexed [{}].[{}].", entityFullName, entityId);
	}

	/**
	 * Reindexes all data in a {@link org.molgenis.data.Repository}
	 *
	 * @param entityMetaData
	 *            the {@link EntityMetaData} of the {@link org.molgenis.data.Repository} to reindex.
	 */
	private void rebuildIndexBatchEntities(EntityMetaData entityMetaData)
	{
		String name = entityMetaData.getName();
		LOG.debug("Reindexing [{}]...", name);
		this.searchService.rebuildIndex(dataService.getRepository(name), entityMetaData);
		LOG.info("Reindexed [{}].", name);
	}

	/**
	 * Retrieves the query to get all reindex actions sorted
	 */
	static Query<Entity> createQueryGetAllReindexActions(String transactionId)
	{
		QueryRule rule = new QueryRule(REINDEX_ACTION_GROUP, EQUALS, transactionId);
		QueryImpl<Entity> q = new QueryImpl<>(rule);
		q.setSort(new Sort(ACTION_ORDER));
		return q;
	}
}