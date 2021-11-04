package org.molgenis.data.index.bootstrap;

import static java.util.Arrays.asList;
import static org.molgenis.data.index.meta.IndexActionMetadata.INDEX_ACTION;
import static org.molgenis.data.index.meta.IndexActionMetadata.INDEX_STATUS;
import static org.molgenis.data.index.meta.IndexActionMetadata.IndexStatus.PENDING;
import static org.molgenis.data.index.meta.IndexActionMetadata.IndexStatus.STARTED;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.util.EntityUtils.getTypedValue;

import org.molgenis.data.DataService;
import org.molgenis.data.index.IndexActionRegisterService;
import org.molgenis.data.index.IndexService;
import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.index.meta.IndexActionMetadata.IndexStatus;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IndexBootstrapper {

  private static final Logger LOG = LoggerFactory.getLogger(IndexBootstrapper.class);

  private final MetaDataService metaDataService;
  private final IndexService indexService;
  private final IndexActionRegisterService indexActionRegisterService;
  private final DataService dataService;
  private final AttributeMetadata attrMetadata;

  public IndexBootstrapper(
      MetaDataService metaDataService,
      IndexService indexService,
      IndexActionRegisterService indexActionRegisterService,
      DataService dataService,
      AttributeMetadata attrMetadata) {
    this.metaDataService = metaDataService;
    this.indexService = indexService;
    this.indexActionRegisterService = indexActionRegisterService;
    this.dataService = dataService;
    this.attrMetadata = attrMetadata;
  }

  public void bootstrap() {
    if (!indexService.hasIndex(attrMetadata)) {
      LOG.debug(
          "No index for Attribute found, assuming missing index, schedule (re)index for all entities");
      metaDataService
          .getRepositories()
          .forEach(repo -> indexActionRegisterService.register(repo.getEntityType(), null));
      LOG.debug("Done scheduling (re)index jobs for all entities");
    } else {
      LOG.debug("Index for Attribute found, index is present, no (re)index needed");
      dataService
          .findAll(
              INDEX_ACTION,
              new QueryImpl<IndexAction>().in(INDEX_STATUS, asList(STARTED, PENDING)),
              IndexAction.class)
          .filter(this::failIndexAction)
          .forEach(this::registerNewIndexAction);
    }
  }

  private boolean failIndexAction(IndexAction action) {
    action.setIndexStatus(IndexStatus.FAILED);
    dataService.update(INDEX_ACTION, action);
    return true;
  }

  private void registerNewIndexAction(IndexAction action) {
    LOG.info("Indexing of {}{} was interrupted during shutdown, rescheduling... ",
        action.getEntityTypeId(),
        action.getEntityId() != null ? "." + action.getEntityId() : "");

    String entityTypeId = action.getEntityTypeId();
    EntityType entityType =
        dataService.findOneById(ENTITY_TYPE_META_DATA, entityTypeId, EntityType.class);
    if (entityType != null) {
      Object typedEntityId = getTypedValue(action.getEntityId(), entityType.getIdAttribute());
      indexActionRegisterService.register(entityType, typedEntityId);
    }
  }
}
