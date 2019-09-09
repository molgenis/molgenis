package org.molgenis.data.index.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.index.job.IndexJobExecutionMetadata.INDEX_JOB_EXECUTION;
import static org.molgenis.data.index.meta.IndexActionGroupMetadata.INDEX_ACTION_GROUP;
import static org.molgenis.data.util.MolgenisDateFormat.parseInstant;

import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.index.IndexActionRegisterServiceImpl;
import org.molgenis.data.index.IndexConfig;
import org.molgenis.data.index.IndexService;
import org.molgenis.data.index.config.IndexTestConfig;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.transaction.TransactionListener;
import org.molgenis.data.transaction.TransactionManager;
import org.molgenis.jobs.JobExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mail.MailSender;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {IndexJobSchedulerTest.Config.class, IndexTestConfig.class})
class IndexJobSchedulerTest extends AbstractMolgenisSpringTest {
  @Autowired private DataService dataService;

  @Autowired private TransactionManager transactionManager;

  @Autowired private TransactionListener molgenisTransactionListener;

  @Autowired private JobExecutor jobExecutor;

  @Mock private Repository<Entity> repository;

  @Autowired private IndexJobScheduler indexJobScheduler;

  @Mock private Stream<Entity> jobExecutions;

  @Captor private ArgumentCaptor<IndexJobExecution> indexJobExecutionCaptor;

  @Captor private ArgumentCaptor<Query<Entity>> queryCaptor;

  @Captor private ArgumentCaptor<Runnable> runnableArgumentCaptor;

  @Autowired private Config config;

  @BeforeEach
  void beforeMethod() throws Exception {
    config.resetMocks();
  }

  @SuppressWarnings("unchecked")
  @Test
  void testRebuildIndexDoesNothingIfNoIndexActionJobIsFound() throws Exception {
    when(dataService.findOneById(INDEX_ACTION_GROUP, "abcde")).thenReturn(null);

    indexJobScheduler.scheduleIndexJob("abcde");

    verify(jobExecutor, never()).submit(any());
  }

  @Test
  void testCleanupJobExecutions() throws Exception {
    when(dataService.getRepository(INDEX_JOB_EXECUTION)).thenReturn(repository);
    when(repository.query()).thenReturn(new QueryImpl<>(repository));
    when(repository.findAll(queryCaptor.capture())).thenReturn(jobExecutions);
    when(dataService.hasRepository(INDEX_JOB_EXECUTION)).thenReturn(true);

    indexJobScheduler.cleanupJobExecutions();

    verify(dataService).delete(INDEX_JOB_EXECUTION, jobExecutions);

    Query<Entity> actualQuery = queryCaptor.getValue();
    Pattern queryPattern =
        Pattern.compile("rules=\\['endDate' < '(.*)', AND, 'status' = 'SUCCESS'\\]");
    Matcher queryMatcher = queryPattern.matcher(actualQuery.toString());
    assertTrue(queryMatcher.matches());

    // check the endDate time limit in the query
    assertEquals(
        Duration.between(parseInstant(queryMatcher.group(1)), Instant.now()).toMinutes(), 5);
  }

  @Configuration
  @Import({IndexConfig.class, IndexActionRegisterServiceImpl.class})
  static class Config {
    @Mock private JobExecutor jobExecutor;

    @Mock private MailSender mailSender;

    @Mock private TransactionManager transactionManager;

    @Mock private IndexService indexService;

    Config() {
      org.mockito.MockitoAnnotations.initMocks(this);
    }

    private void resetMocks() {
      reset(jobExecutor, mailSender, transactionManager, indexService);
    }

    @Bean
    JobExecutor jobExecutor() {
      return jobExecutor;
    }

    @Bean
    IndexService indexService() {
      return indexService;
    }

    @Bean
    TransactionManager molgenisTransactionManager() {
      return transactionManager;
    }

    @Bean
    MailSender mailSender() {
      return mailSender;
    }
  }
}
