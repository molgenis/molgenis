package org.molgenis.data.index;

import static java.time.Duration.between;
import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.index.meta.IndexActionMetadata.INDEX_ACTION;
import static org.molgenis.data.index.meta.IndexActionMetadata.TRANSACTION_ID;
import static org.molgenis.data.util.MolgenisDateFormat.parseInstant;

import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.AbstractMockitoTest;

class IndexActionSchedulerTest extends AbstractMockitoTest {

  @Mock private DataService dataService;

  @Mock private ExecutorService executorService;

  @Mock private Repository<Entity> repository;

  @Mock private Stream<Entity> jobExecutions;
  @Mock private IndexActionService indexActionService;

  @Captor private ArgumentCaptor<Query<Entity>> queryCaptor;

  private IndexActionSchedulerImpl indexActionScheduler;

  @BeforeEach
  void beforeMethod() {
    indexActionScheduler =
        new IndexActionSchedulerImpl(indexActionService, executorService, dataService);
  }

  @Test
  void testRebuildIndexDoesNothingIfNoIndexActionsAreFound() {
    when(dataService.findAll(
            INDEX_ACTION,
            new QueryImpl<IndexAction>().eq(TRANSACTION_ID, "abcde"),
            IndexAction.class))
        .thenReturn(Stream.of());

    indexActionScheduler.scheduleIndexActions("abcde");

    verify(executorService, never()).execute(any());
  }

  @Test
  void testSchedule() {
    var indexAction1 = mock(IndexAction.class);
    var indexAction2 = mock(IndexAction.class);
    when(indexAction1.getEntityTypeId()).thenReturn("entityType1");
    when(indexAction2.getEntityTypeId()).thenReturn("entityType2");

    indexActionScheduler.schedule(indexAction1);
    indexActionScheduler.schedule(indexAction2);

    verify(executorService, times(2)).execute(any());
  }

  @Test
  void testScheduleExistingWork() {
    var indexAction1 = mock(IndexAction.class);
    var indexAction2 = mock(IndexAction.class);
    when(indexAction1.getEntityTypeId()).thenReturn("entityType1");
    when(indexAction2.getEntityTypeId()).thenReturn("entityType1");

    indexActionScheduler.schedule(indexAction1);
    indexActionScheduler.schedule(indexAction2);

    verify(executorService, times(1)).execute(any());
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
}
