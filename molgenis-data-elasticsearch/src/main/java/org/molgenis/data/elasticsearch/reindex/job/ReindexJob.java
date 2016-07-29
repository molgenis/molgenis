package org.molgenis.data.elasticsearch.reindex.job;

import org.molgenis.data.*;
import org.molgenis.data.elasticsearch.ElasticsearchService.IndexingMode;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.reindex.meta.ReindexAction;
import org.molgenis.data.reindex.meta.ReindexActionGroup;
import org.molgenis.data.reindex.meta.ReindexActionGroupMetaData;
import org.molgenis.data.reindex.meta.ReindexActionMetaData;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.*;
import org.molgenis.data.support.QueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

import java.util.List;

import static java.text.MessageFormat.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.QueryRule.Operator.EQUALS;
import static org.molgenis.data.reindex.meta.ReindexActionGroupMetaData.REINDEX_ACTION_GROUP;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.*;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType.CREATE;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType.DELETE;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.DataType.DATA;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.DataType.METADATA;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.ReindexStatus.*;

/**
 * {@link Job} that executes a bunch of {@link ReindexActionMetaData} stored in a {@link ReindexActionGroupMetaData}.
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
		ReindexActionGroup reindexActionGroup = dataService
				.findOneById(REINDEX_ACTION_GROUP, transactionId, ReindexActionGroup.class);
		if (reindexActionGroup != null && reindexActionGroup.getCount() > 0)
		{
			progress.setProgressMax(reindexActionGroup.getCount());
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
		List<ReindexAction> reindexActions = dataService
				.findAll(REINDEX_ACTION, createQueryGetAllReindexActions(transactionId), ReindexAction.class)
				.collect(toList());
		try
		{
			boolean success = true;
			int count = 0;
			for (ReindexAction reindexAction : reindexActions)
			{
				success &= performAction(progress, count++, reindexAction);
			}
			if (success)
			{
				progress.progress(count, "Executed all reindex actions, cleaning up the actions...");
				dataService.delete(REINDEX_ACTION, reindexActions.stream());
				dataService.deleteById(REINDEX_ACTION_GROUP, transactionId);
				progress.progress(count, "Cleaned up the actions.");
			}
		}
		catch (Exception ex)
		{
			LOG.error("Error performing reindexActions", ex);
			throw ex;
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
	 * @param progress      {@link Progress} to report progress to
	 * @param progressCount the progress count for this ReindexAction
	 * @param reindexAction Entity of type ReindexActionMetaData
	 * @return boolean indicating success or failure
	 */
	private boolean performAction(Progress progress, int progressCount, ReindexAction reindexAction)
	{
		requireNonNull(reindexAction.getEntityMetaData());

		updateReindexActionStatus(reindexAction, STARTED);

		try
		{
			if (reindexAction.getEntityId() != null)
			{
				progress.progress(progressCount,
						format("Reindexing {0}.{1}, CUDType = {2}", reindexAction.getEntityFullName(),
								reindexAction.getEntityId(), reindexAction.getCudType()));
				rebuildIndexOneEntity(reindexAction.getEntityFullName(), reindexAction.getEntityId(),
						reindexAction.getCudType());
			}
			else if (reindexAction.getDataType().equals(METADATA) && reindexAction.getCudType() == CREATE)
			{
				progress.progress(progressCount,
						format("Create index mappings {0}. CUDType = {1}", reindexAction.getEntityFullName(),
								reindexAction.getCudType()));

				String entityFullName = reindexAction.getEntityFullName();
				EntityMetaData entityMeta = dataService.getEntityMetaData(entityFullName);
				searchService.createMappings(entityMeta);
			}
			else if (reindexAction.getDataType().equals(DATA) || reindexAction.getCudType() != DELETE)
			{
				progress.progress(progressCount,
						format("Reindexing repository {0}. CUDType = {1}", reindexAction.getEntityFullName(),
								reindexAction.getCudType()));
				rebuildIndexBatchEntities(reindexAction.getEntityFullName());
			}
			else
			{
				progress.progress(progressCount,
						format("Dropping index of repository {0}.", reindexAction.getEntityFullName()));
				searchService.delete(reindexAction.getEntityFullName());
			}
			updateReindexActionStatus(reindexAction, FINISHED);
			return true;
		}
		catch (Exception ex)
		{
			LOG.error("Reindex job failed", ex);
			updateReindexActionStatus(reindexAction, FAILED);
			return false;
		}
	}

	/**
	 * Updates the {@link ReindexStatus} of a ReindexAction and stores the change.
	 *
	 * @param reindexAction the ReindexAction of which the status is updated
	 * @param status        the new {@link ReindexStatus}
	 */
	private void updateReindexActionStatus(ReindexAction reindexAction, ReindexStatus status)
	{
		reindexAction.setReindexStatus(status);
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
		//FIXME: Deze is gedecorate, kan in foute gevallen dus de IDs uit de index halen
		final Repository<Entity> repository = dataService.getRepository(entityFullName);
		searchService.rebuildIndex(repository);
		LOG.info("Reindexed [{}].", entityFullName);
	}

	/**
	 * Retrieves the query to get all reindex actions sorted
	 */
	static Query<ReindexAction> createQueryGetAllReindexActions(String transactionId)
	{
		QueryRule rule = new QueryRule(REINDEX_ACTION_GROUP_ATTR, EQUALS, transactionId);
		QueryImpl<ReindexAction> q = new QueryImpl<>(rule);
		q.setSort(new Sort(ACTION_ORDER));
		return q;
	}
}