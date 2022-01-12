package org.molgenis.data.index;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.index.meta.IndexActionMetadata.INDEX_ACTION;

import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import org.molgenis.data.DataService;
import org.molgenis.data.index.job.IndexActionScheduler;
import org.molgenis.data.index.job.IndexActionSchedulerImpl;
import org.molgenis.data.index.job.IndexActionService;
import org.molgenis.data.index.meta.IndexActionFactory;
import org.molgenis.data.index.meta.IndexActionMetadata;
import org.molgenis.data.index.meta.IndexPackage;
import org.molgenis.data.index.transaction.IndexTransactionListener;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.transaction.TransactionManager;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

// TODO: These imported classes should be in separate config and this is the IndexJobConfig
@Import({
  IndexActionFactory.class,
  IndexActionMetadata.class,
  IndexPackage.class,
  JobPackage.class,
  JobExecutionMetaData.class,
  IndexActionRegisterServiceImpl.class,
  IndexingStrategy.class
})
@Configuration
public class IndexConfig {
  private final IndexActionRegisterService indexActionRegisterService;
  private final TransactionManager transactionManager;
  private final DataService dataService;
  private final IndexService indexService;
  private final EntityTypeFactory entityTypeFactory;

  public IndexConfig(
      IndexActionRegisterService indexActionRegisterService,
      TransactionManager transactionManager,
      DataService dataService,
      IndexService indexService,
      EntityTypeFactory entityTypeFactory) {
    this.indexActionRegisterService = requireNonNull(indexActionRegisterService);
    this.transactionManager = requireNonNull(transactionManager);
    this.dataService = requireNonNull(dataService);
    this.indexService = requireNonNull(indexService);
    this.entityTypeFactory = requireNonNull(entityTypeFactory);
  }

  @PostConstruct
  public void register() {
    indexActionRegisterService.addExcludedEntity(INDEX_ACTION);
  }

  @Bean
  public IndexTransactionListener indexTransactionListener(
      IndexActionScheduler indexActionScheduler) {
    final IndexTransactionListener indexTransactionListener =
        new IndexTransactionListener(indexActionScheduler, indexActionRegisterService);
    transactionManager.addTransactionListener(indexTransactionListener);
    return indexTransactionListener;
  }

  @Bean
  public IndexActionScheduler indexJobScheduler(IndexActionService indexActionService) {
    var executors = Executors.newFixedThreadPool(5);
    return new IndexActionSchedulerImpl(indexActionService, executors, dataService);
  }

  @Bean
  public IndexActionService indexJobService() {
    return new IndexActionService(dataService, indexService, entityTypeFactory);
  }
}
