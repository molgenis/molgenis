package org.molgenis.data.index;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.index.meta.IndexActionMetadata.INDEX_ACTION;
import static org.molgenis.data.index.meta.IndexActionMetadata.IndexStatus.FAILED;
import static org.molgenis.data.index.meta.IndexActionMetadata.IndexStatus.FINISHED;
import static org.molgenis.data.util.EntityUtils.getTypedValue;

import io.micrometer.core.annotation.Timed;
import java.time.Instant;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.index.meta.IndexActionMetadata;
import org.molgenis.data.index.meta.IndexActionMetadata.IndexStatus;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Executes {@link IndexAction}s. */
public class IndexActionService {
  private static final Logger LOG = LoggerFactory.getLogger(IndexActionService.class);

  private final DataService dataService;
  private final IndexService indexService;
  private final EntityTypeFactory entityTypeFactory;

  public IndexActionService(
      DataService dataService, IndexService indexService, EntityTypeFactory entityTypeFactory) {
    this.dataService = requireNonNull(dataService);
    this.indexService = requireNonNull(indexService);
    this.entityTypeFactory = requireNonNull(entityTypeFactory);
  }

  /**
   * Performs a single IndexAction
   *
   * @param indexAction Entity of type IndexAction
   * @return boolean indicating success or failure
   */
  @Timed(
      value = "service.index",
      description = "Timing information for the index service.",
      histogram = true)
  @RunAsSystem
  public boolean performAction(IndexAction indexAction) {
    requireNonNull(indexAction);
    String entityTypeId = indexAction.getEntityTypeId();
    try {
      if (dataService.hasEntityType(entityTypeId)) {
        EntityType entityType = dataService.getEntityType(entityTypeId);
        if (indexAction.getEntityId() != null) {
          LOG.info("Indexing {}.{}", entityType.getId(), indexAction.getEntityId());
          rebuildIndexOneEntity(entityTypeId, indexAction.getEntityId());
        } else {
          LOG.info("Indexing {}", entityType.getId());
          final Repository<Entity> repository = dataService.getRepository(entityType.getId());
          indexService.rebuildIndex(repository);
        }
      } else {
        EntityType entityType = getEntityType(indexAction);
        if (indexService.hasIndex(entityType)) {
          LOG.info("Dropping entityType with id: {}", entityType.getId());
          indexService.deleteIndex(entityType);
        } else {
          // Index action is finished, here we concluded that we don't have enough info to continue
          LOG.info("Skip index entity {}.{}", entityType.getId(), indexAction.getEntityId());
        }
      }
      return true;
    } catch (Exception ex) {
      LOG.error("Index action failed", ex);
      return false;
    }
  }

  /**
   * Updates the {@link IndexStatus} of an IndexAction and stores the change. Also sets the end date
   * if the action has finished in any way.
   *
   * @param indexAction the IndexAction of which the status is updated
   * @param status the new {@link IndexStatus}
   */
  @RunAsSystem
  public void updateIndexActionStatus(
      IndexAction indexAction, IndexActionMetadata.IndexStatus status) {
    if (status == FAILED || status == FINISHED) {
      indexAction.setEndDateTime(Instant.now());
    }
    indexAction.setIndexStatus(status);
    dataService.update(INDEX_ACTION, indexAction);
  }

  /**
   * Indexes one single entity instance.
   *
   * @param entityTypeId the id of the entity's repository
   * @param untypedEntityId the identifier of the entity to update
   */
  private void rebuildIndexOneEntity(String entityTypeId, String untypedEntityId) {
    LOG.trace("Indexing [{}].[{}]... ", entityTypeId, untypedEntityId);

    // convert entity id string to typed entity id
    EntityType entityType = dataService.getEntityType(entityTypeId);
    if (null != entityType) {
      Object entityId = getTypedValue(untypedEntityId, entityType.getIdAttribute());
      String entityFullName = entityType.getId();

      Entity actualEntity = dataService.findOneById(entityFullName, entityId);

      if (null == actualEntity) {
        // Delete
        LOG.debug("Index delete [{}].[{}].", entityFullName, entityId);
        indexService.deleteById(entityType, entityId);
        return;
      }

      boolean indexEntityExists = indexService.hasIndex(entityType);
      if (!indexEntityExists) {
        LOG.debug("Create mapping of repository [{}] because it was not exist yet", entityTypeId);
        indexService.createIndex(entityType);
      }

      LOG.debug("Index [{}].[{}].", entityTypeId, entityId);
      indexService.index(actualEntity.getEntityType(), actualEntity);
    } else {
      throw new MolgenisDataException("Unknown EntityType for entityTypeId: " + entityTypeId);
    }
  }

  private EntityType getEntityType(IndexAction indexAction) {
    return entityTypeFactory.create(indexAction.getEntityTypeId());
  }
}
