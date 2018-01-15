package org.molgenis.data.security.aggregation;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.settings.AppSettings;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class AggregateAnonymizerRepositoryDecoratorTest
{
	private AggregateAnonymizerRepositoryDecorator aggregateAnonymizerRepoDecorator;
	private Repository<Entity> delegateRepository;
	private AggregateAnonymizer aggregateAnonymizer;
	private AppSettings appSettings;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		delegateRepository = mock(Repository.class);
		aggregateAnonymizer = mock(AggregateAnonymizer.class);
		appSettings = mock(AppSettings.class);
		aggregateAnonymizerRepoDecorator = new AggregateAnonymizerRepositoryDecorator<>(delegateRepository,
				aggregateAnonymizer, appSettings);
	}

	@Test
	public void aggregateNoThreshold() throws Exception
	{
		when(appSettings.getAggregateThreshold()).thenReturn(null);
		AggregateQuery aggregateQuery = mock(AggregateQuery.class);
		AggregateResult aggregateResult = mock(AggregateResult.class);
		when(delegateRepository.aggregate(aggregateQuery)).thenReturn(aggregateResult);
		assertEquals(aggregateResult, aggregateAnonymizerRepoDecorator.aggregate(aggregateQuery));
		verifyZeroInteractions(aggregateAnonymizer);
		verifyZeroInteractions(aggregateResult);
	}

	@Test
	public void aggregateThreshold() throws Exception
	{
		int threshold = 10;
		when(appSettings.getAggregateThreshold()).thenReturn(threshold);
		AggregateQuery aggregateQuery = mock(AggregateQuery.class);
		AggregateResult aggregateResult = mock(AggregateResult.class);
		when(delegateRepository.aggregate(aggregateQuery)).thenReturn(aggregateResult);
		AnonymizedAggregateResult anonymizedAggregateResult = mock(AnonymizedAggregateResult.class);
		when(aggregateAnonymizer.anonymize(aggregateResult, threshold)).thenReturn(anonymizedAggregateResult);
		assertEquals(anonymizedAggregateResult, aggregateAnonymizerRepoDecorator.aggregate(aggregateQuery));
	}
}