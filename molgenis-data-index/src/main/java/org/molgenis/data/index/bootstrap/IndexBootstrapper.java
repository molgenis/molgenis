package org.molgenis.data.index.bootstrap;

import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.util.EntityUtils.getTypedValue;
import static org.molgenis.jobs.model.JobExecutionMetaData.FAILED;

import java.util.List;
import java.util.stream.Collectors;
import org.molgenis.data.DataService;
import org.molgenis.data.index.IndexActionRegisterService;
import org.molgenis.data.index.IndexService;
import org.molgenis.data.index.job.IndexJobExecution;
import org.molgenis.data.index.job.IndexJobExecutionMetadata;
import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.index.meta.IndexActionGroupMetadata;
import org.molgenis.data.index.meta.IndexActionMetadata;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.jobs.model.JobExecutionMetaData;
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
          "No index for Attribute found, asuming missing index, schedule (re)index for all entities");
      metaDataService
          .getRepositories()
          .forEach(repo -> indexActionRegisterService.register(repo.getEntityType(), null));
      LOG.debug("Done scheduling (re)index jobs for all entities");
    } else {
      LOG.debug("Index for Attribute found, index is present, no (re)index needed");
      List<IndexJobExecution> failedIndexJobs =
          dataService
              .findAll(
                  IndexJobExecutionMetadata.INDEX_JOB_EXECUTION,
                  new QueryImpl<IndexJobExecution>().eq(JobExecutionMetaData.STATUS, FAILED),
                  IndexJobExecution.class)
              .collect(Collectors.toList());
      failedIndexJobs.forEach(this::registerNewIndexActionForDirtyJobs);
    }
  }

  private void registerNewIndexActionForDirtyJobs(IndexJobExecution indexJobExecution) {
    String id = indexJobExecution.getIndexActionJobID();
    List<IndexAction> actions =
        dataService
            .findAll(
                IndexActionMetadata.INDEX_ACTION,
                new QueryImpl<IndexAction>().eq(IndexActionMetadata.INDEX_ACTION_GROUP_ATTR, id),
                IndexAction.class)
            .collect(Collectors.toList());
    actions.forEach(this::registerIndexAction);

    dataService.delete(IndexJobExecutionMetadata.INDEX_JOB_EXECUTION, indexJobExecution);
    dataService.deleteAll(
        IndexActionMetadata.INDEX_ACTION, actions.stream().map(IndexAction::getId));
    dataService.deleteById(IndexActionGroupMetadata.INDEX_ACTION_GROUP, id);
  }

  private void registerIndexAction(IndexAction action) {
    String entityTypeId = action.getEntityTypeId();
    EntityType entityType =
        dataService.findOneById(ENTITY_TYPE_META_DATA, entityTypeId, EntityType.class);
    if (entityType != null) {
      Object typedEntityId = getTypedValue(action.getEntityId(), entityType.getIdAttribute());
      indexActionRegisterService.register(entityType, typedEntityId);
    }
  }
}
