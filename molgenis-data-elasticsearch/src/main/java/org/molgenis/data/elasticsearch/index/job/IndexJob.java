package org.molgenis.data.elasticsearch.index.job;

import org.molgenis.data.*;
import org.molgenis.data.elasticsearch.ElasticsearchService.IndexingMode;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.index.meta.IndexActionGroup;
import org.molgenis.data.index.meta.IndexActionGroupMetaData;
import org.molgenis.data.index.meta.IndexActionMetaData;
import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.QueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

import java.util.List;

import static java.text.MessageFormat.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.QueryRule.Operator.EQUALS;
import static org.molgenis.data.index.meta.IndexActionGroupMetaData.INDEX_ACTION_GROUP;
import static org.molgenis.data.index.meta.IndexActionMetaData.*;
import static org.molgenis.util.EntityUtils.getTypedValue;

/**
 * {@link Job} that executes a bunch of {@link IndexActionMetaData} stored in a {@link IndexActionGroupMetaData}.
 */
class IndexJob extends Job
{
	private static final Logger LOG = LoggerFactory.getLogger(IndexJob.class);
	private final String transactionId;
	private final DataService dataService;
	private final SearchService searchService;
	private final EntityTypeFactory entityTypeFactory;

	IndexJob(Progress progress, Authentication authentication, String transactionId, DataService dataService,
			SearchService searchService, EntityTypeFactory entityTypeFactory)
	{
		super(progress, null, authentication);
		this.transactionId = requireNonNull(transactionId);
		this.dataService = requireNonNull(dataService);
		this.searchService = requireNonNull(searchService);
		this.entityTypeFactory = requireNonNull(entityTypeFactory);
	}

	@Override
	public Void call(Progress progress)
	{
		requireNonNull(progress);
		IndexActionGroup indexActionGroup = dataService
				.findOneById(INDEX_ACTION_GROUP, transactionId, IndexActionGroup.class);
		if (indexActionGroup != null && indexActionGroup.getCount() > 0)
		{
			progress.setProgressMax(indexActionGroup.getCount());
			progress.status(format("Start indexing for transaction id: [{0}]", transactionId));
			performIndexActions(progress);
			progress.status(format("Finished indexing for transaction id: [{0}]", transactionId));
		}
		else
		{
			progress.status(format("No index actions found for transaction id: [{0}]", transactionId));
		}
		return null;
	}

	/**
	 * Performs the IndexActions.
	 *
	 * @param progress {@link Progress} instance to log progress information to
	 */
	private void performIndexActions(Progress progress)
	{
		List<IndexAction> indexActions = dataService
				.findAll(INDEX_ACTION, createQueryGetAllIndexActions(transactionId), IndexAction.class)
				.collect(toList());
		try
		{
			boolean success = true;
			int count = 0;
			for (IndexAction indexAction : indexActions)
			{
				success &= performAction(progress, count++, indexAction);
			}
			if (success)
			{
				progress.progress(count, "Executed all index actions, cleaning up the actions...");
				dataService.delete(INDEX_ACTION, indexActions.stream());
				dataService.deleteById(INDEX_ACTION_GROUP, transactionId);
				progress.progress(count, "Cleaned up the actions.");
			}
		}
		catch (Exception ex)
		{
			LOG.error("Error performing index actions", ex);
			throw ex;
		}
		finally
		{
			progress.status("Refresh index start");
			searchService.refreshIndex();
			progress.status("Refresh index done");
		}
	}

	/**
	 * Performs a single IndexAction
	 *
	 * @param progress      {@link Progress} to report progress to
	 * @param progressCount the progress count for this IndexAction
	 * @param indexAction   Entity of type IndexActionMetaData
	 * @return boolean indicating success or failure
	 */
	private boolean performAction(Progress progress, int progressCount, IndexAction indexAction)
	{
		requireNonNull(indexAction);
		String entityTypeId = indexAction.getEntityTypeId();
		updateIndexActionStatus(indexAction, IndexActionMetaData.IndexStatus.STARTED);
		EntityType entityType = dataService.getEntityType(entityTypeId);
		try
		{
			if (entityType != null)
			{
				if (indexAction.getEntityId() != null)
				{
					progress.progress(progressCount,
							format("Indexing {0}.{1}", entityType.getId(), indexAction.getEntityId()));
					rebuildIndexOneEntity(entityTypeId, indexAction.getEntityId());
				}
				else
				{
					progress.progress(progressCount, format("Indexing {0}", entityType.getId()));
					final Repository<Entity> repository = dataService.getRepository(entityType.getId());
					searchService.rebuildIndex(repository);
				}
			}
			else
			{
				entityType = getEntityType(indexAction);
				if (searchService.hasMapping(entityType))
				{
					progress.progress(progressCount, format("Dropping entityType with id: {0}", entityType.getId()));
					searchService.delete(entityType);
				}
				else
				{
					// Index Job is finished, here we concluded that we don't have enough info to continue the index job
					progress.progress(progressCount, format("Skip index entity {0}.{1}", entityType.getId(),
									indexAction.getEntityId()));
				}
			}
			updateIndexActionStatus(indexAction, IndexActionMetaData.IndexStatus.FINISHED);
			return true;
		}
		catch (Exception ex)
		{
			LOG.error("Index job failed", ex);
			updateIndexActionStatus(indexAction, IndexActionMetaData.IndexStatus.FAILED);
			return false;
		}
	}

	/**
	 * Updates the {@link IndexStatus} of a IndexAction and stores the change.
	 *
	 * @param indexAction the IndexAction of which the status is updated
	 * @param status      the new {@link IndexStatus}
	 */
	private void updateIndexActionStatus(IndexAction indexAction, IndexActionMetaData.IndexStatus status)
	{
		indexAction.setIndexStatus(status);
		dataService.update(INDEX_ACTION, indexAction);
	}

	/**
	 * Indexes one single entity instance.
	 *
	 * @param entityTypeId the id of the entity's repository
	 * @param untypedEntityId the identifier of the entity to update
	 */
	private void rebuildIndexOneEntity(String entityTypeId, String untypedEntityId)
	{
		LOG.trace("Indexing [{}].[{}]... ", entityTypeId, untypedEntityId);

		// convert entity id string to typed entity id
		EntityType entityType = dataService.getEntityType(entityTypeId);
		if (null != entityType)
		{
			Object entityId = getTypedValue(untypedEntityId, entityType.getIdAttribute());
			String entityFullName = entityType.getId();

			Entity actualEntity = dataService.findOneById(entityFullName, entityId);

			if (null == actualEntity)
			{
				// Delete
				LOG.debug("Index delete [{}].[{}].", entityFullName, entityId);
				searchService.deleteById(entityId.toString(), entityType);
				return;
			}

			boolean indexEntityExists = searchService.hasMapping(entityType);
			if (!indexEntityExists)
			{
				LOG.debug("Create mapping of repository [{}] because it was not exist yet", entityTypeId);
				searchService.createMappings(entityType);
			}

			Query<Entity> q = new QueryImpl<>();
			q.eq(entityType.getIdAttribute().getName(), entityId);
			Entity indexEntity = searchService.findOne(q, entityType);

			if (null != indexEntity)
			{
				// update
				LOG.debug("Index update [{}].[{}].", entityTypeId, entityId);
				searchService.index(actualEntity, actualEntity.getEntityType(), IndexingMode.UPDATE);
			}
			else
			{
				// Add
				LOG.debug("Index add [{}].[{}].", entityTypeId, entityId);
				searchService.index(actualEntity, actualEntity.getEntityType(), IndexingMode.ADD);
			}
		}
		else
		{
			throw new MolgenisDataException("Unknown EntityType for entityTypeId: " + entityTypeId);
		}
	}

	/**
	 * Retrieves the query to get all index actions sorted
	 */
	static Query<IndexAction> createQueryGetAllIndexActions(String transactionId)
	{
		QueryRule rule = new QueryRule(INDEX_ACTION_GROUP_ATTR, EQUALS, transactionId);
		QueryImpl<IndexAction> q = new QueryImpl<>(rule);
		q.setSort(new Sort(ACTION_ORDER));
		return q;
	}

	private EntityType getEntityType(IndexAction indexAction)
	{
		return entityTypeFactory.create(indexAction.getEntityTypeId());
	}
}