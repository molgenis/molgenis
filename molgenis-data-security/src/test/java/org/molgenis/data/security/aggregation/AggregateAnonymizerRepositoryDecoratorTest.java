package org.molgenis.data.security.aggregation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.settings.AppSettings;

class AggregateAnonymizerRepositoryDecoratorTest {
  private AggregateAnonymizerRepositoryDecorator aggregateAnonymizerRepoDecorator;
  private Repository<Entity> delegateRepository;
  private AggregateAnonymizer aggregateAnonymizer;
  private AppSettings appSettings;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUpBeforeMethod() {
    delegateRepository = mock(Repository.class);
    aggregateAnonymizer = mock(AggregateAnonymizer.class);
    appSettings = mock(AppSettings.class);
    aggregateAnonymizerRepoDecorator =
        new AggregateAnonymizerRepositoryDecorator<>(
            delegateRepository, aggregateAnonymizer, appSettings);
  }

  @Test
  void aggregateNoThreshold() throws Exception {
    when(appSettings.getAggregateThreshold()).thenReturn(null);
    AggregateQuery aggregateQuery = mock(AggregateQuery.class);
    AggregateResult aggregateResult = mock(AggregateResult.class);
    when(delegateRepository.aggregate(aggregateQuery)).thenReturn(aggregateResult);
    assertEquals(aggregateResult, aggregateAnonymizerRepoDecorator.aggregate(aggregateQuery));
    verifyZeroInteractions(aggregateAnonymizer);
    verifyZeroInteractions(aggregateResult);
  }

  @Test
  void aggregateThreshold() throws Exception {
    int threshold = 10;
    when(appSettings.getAggregateThreshold()).thenReturn(threshold);
    AggregateQuery aggregateQuery = mock(AggregateQuery.class);
    AggregateResult aggregateResult = mock(AggregateResult.class);
    when(delegateRepository.aggregate(aggregateQuery)).thenReturn(aggregateResult);
    AnonymizedAggregateResult anonymizedAggregateResult = mock(AnonymizedAggregateResult.class);
    when(aggregateAnonymizer.anonymize(aggregateResult, threshold))
        .thenReturn(anonymizedAggregateResult);
    assertEquals(
        anonymizedAggregateResult, aggregateAnonymizerRepoDecorator.aggregate(aggregateQuery));
  }
}
