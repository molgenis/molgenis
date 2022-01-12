package org.molgenis.data.index.job;

import static java.time.Duration.between;
import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.index.meta.IndexActionMetadata.INDEX_ACTION;
import static org.molgenis.data.util.MolgenisDateFormat.parseInstant;

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
import org.molgenis.data.transaction.TransactionManager;
import org.molgenis.jobs.JobExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mail.MailSender;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {IndexActionSchedulerTest.Config.class, IndexTestConfig.class})
class IndexActionSchedulerTest extends AbstractMolgenisSpringTest {

  @Autowired private DataService dataService;

  @Autowired private JobExecutor jobExecutor;

  @Mock private Repository<Entity> repository;

  @Autowired private IndexActionScheduler indexActionScheduler;

  @Mock private Stream<Entity> jobExecutions;

  @Captor private ArgumentCaptor<Query<Entity>> queryCaptor;

  @Autowired private Config config;

  @BeforeEach
  void beforeMethod() {
    config.resetMocks();
  }

  @Test
  void testRebuildIndexDoesNothingIfNoIndexActionJobIsFound() {
    when(dataService.findOneById(INDEX_ACTION, "abcde")).thenReturn(null);

    indexActionScheduler.scheduleIndexActions("abcde");

    verify(jobExecutor, never()).submit(any());
  }

  @Test
  void testCleanupJobExecutions() {
    when(dataService.getRepository(INDEX_ACTION)).thenReturn(repository);
    when(repository.query()).thenReturn(new QueryImpl<>(repository));
    when(repository.findAll(queryCaptor.capture())).thenReturn(jobExecutions);
    when(dataService.hasRepository(INDEX_ACTION)).thenReturn(true);

    indexActionScheduler.cleanupIndexActions();

    verify(dataService).delete(INDEX_ACTION, jobExecutions);

    Query<Entity> actualQuery = queryCaptor.getValue();
    Pattern queryPattern =
        Pattern.compile(
            "rules=\\[\\('endDateTime' < '(.*)', OR, 'endDateTime' = 'null'\\), AND, !=, 'indexStatus' IN \\[STARTED, PENDING]]");
    Matcher queryMatcher = queryPattern.matcher(actualQuery.toString());
    assertTrue(queryMatcher.matches());

    // check the endDate time limit in the query
    assertEquals(5, between(parseInstant(queryMatcher.group(1)), now()).toMinutes());
  }

  @SuppressWarnings("java:S5979") // mocks are initialized
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
