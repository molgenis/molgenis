package org.molgenis.data.elasticsearch.reindex.job;

import static java.text.MessageFormat.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.QueryRule.Operator.EQUALS;
import static org.molgenis.data.reindex.meta.ReindexActionJobMetaData.COUNT;
import static org.molgenis.data.reindex.meta.ReindexActionJobMetaData.REINDEX_ACTION_JOB;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.ACTION_ORDER;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType.DELETE;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.DataType.DATA;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.REINDEX_ACTION;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.REINDEX_ACTION_GROUP;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.REINDEX_STATUS;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.ReindexStatus.FAILED;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.ReindexStatus.FINISHED;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.ReindexStatus.STARTED;

import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
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
		Entity reindexActionEntity = dataService.findOneById(REINDEX_ACTION_JOB, transactionId);
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
	 * @param progress {@link Progress} instance to log progress information to
	 */
	private void performReindexActions(Progress progress)
	{
		List<Entity> reindexActions = dataService
				.findAll(REINDEX_ACTION, createQueryGetAllReindexActions(transactionId)).collect(toList());
		try
		{
			boolean success = true;
			int count = 0;
			for (Entity reindexAction : reindexActions)
			{
				success &= performAction(progress, count++, reindexAction);
			}
			if (success)
			{
				progress.progress(count, "Executed all reindex actions, cleaning up the actions...");
				dataService.delete(REINDEX_ACTION, reindexActions.stream());
				dataService.deleteById(REINDEX_ACTION_JOB, transactionId);
				progress.progress(count, "Cleaned up the actions.");
			}
		}
		finally
		{
			progress.status("refreshIndex...");
			searchService.refreshIndex();
			progress.status("refreshIndex done.");
		}
	}

	/**
	 * Performs a single ReindexAction
	 *
	 * @param progress            {@link Progress} to report progress to
	 * @param progressCount       the progress count for this ReindexAction
	 * @param reindexActionEntity Entity of type ReindexActionMetaData
	 * @return boolean indicating success or failure
	 */
	private boolean performAction(Progress progress, int progressCount, Entity reindexActionEntity)
	{
		requireNonNull(reindexActionEntity.getEntityMetaData());
		final CudType cudType = CudType.valueOf(reindexActionEntity.getString(ReindexActionMetaData.CUD_TYPE));
		final String entityFullName = reindexActionEntity.getString(ReindexActionMetaData.ENTITY_FULL_NAME);
		final String entityId = reindexActionEntity.getString(ReindexActionMetaData.ENTITY_ID);
		final DataType dataType = DataType.valueOf(reindexActionEntity.getString(ReindexActionMetaData.DATA_TYPE));

		setStatus(reindexActionEntity, STARTED);
		try
		{
			if (entityId != null)
			{
				progress.progress(progressCount,
						format("Reindexing {0}.{1}, CUDType = {2}", entityFullName, entityId, cudType));
				rebuildIndexOneEntity(entityFullName, entityId, cudType);
			}
			else if (dataType.equals(DATA) || cudType != DELETE)
			{
				progress.progress(progressCount,
						format("Reindexing repository {0}. CUDType = {1}", entityFullName, cudType));
				rebuildIndexBatchEntities(entityFullName);
			}
			else
			{
				progress.progress(progressCount, format("Dropping index of repository {0}.", entityFullName));
				searchService.delete(entityFullName);
			}
			setStatus(reindexActionEntity, FINISHED);
			return true;
		}
		catch (Exception ex)
		{
			LOG.error("Reindex job failed", ex);
			setStatus(reindexActionEntity, FAILED);
			return false;
		}
	}

	/**
	 * Updates the {@link ReindexStatus} of a ReindexAction and stores the change.
	 *
	 * @param reindexAction the ReindexAction of which the status is updated
	 * @param status        the new {@link ReindexStatus}
	 */
	private void setStatus(Entity reindexAction, ReindexStatus status)
	{
		reindexAction.set(REINDEX_STATUS, status.toString());
		dataService.update(REINDEX_ACTION, reindexAction);
	}

	/**
	 * Reindexes one single entity instance.
	 *
	 * @param entityFullName the fully qualified name of the entity's repository
	 * @param entityId       the identifier of the entity to update
	 * @param cudType        the {@link CudType} of the change that was made to the entity
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
				searchService.deleteById(entityId, dataService.getMeta().getEntityMetaData(entityFullName));
				break;
			default:
				throw new IllegalStateException("Unknown CudType");
		}
		LOG.info("Reindexed [{}].[{}].", entityFullName, entityId);
	}

	/**
	 * Reindexes all data in a {@link org.molgenis.data.Repository}
	 *
	 * @param entityFullName the fully qualified name of the {@link org.molgenis.data.Repository} to reindex.
	 */
	private void rebuildIndexBatchEntities(String entityFullName)
	{
		LOG.debug("Reindexing [{}]...", entityFullName);
		searchService.rebuildIndex(dataService.getRepository(entityFullName),
				dataService.getMeta().getEntityMetaData(entityFullName));
		LOG.info("Reindexed [{}].", entityFullName);
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