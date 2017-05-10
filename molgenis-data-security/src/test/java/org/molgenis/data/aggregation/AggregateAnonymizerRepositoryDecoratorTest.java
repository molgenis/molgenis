package org.molgenis.data.aggregation;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.settings.AppSettings;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class AggregateAnonymizerRepositoryDecoratorTest
{
	private AggregateAnonymizerRepositoryDecorator aggregateAnonymizerRepoDecorator;
	private Repository<Entity> decoratedRepo;
	private AggregateAnonymizer aggregateAnonymizer;
	private AppSettings appSettings;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepo = mock(Repository.class);
		aggregateAnonymizer = mock(AggregateAnonymizer.class);
		appSettings = mock(AppSettings.class);
		aggregateAnonymizerRepoDecorator = new AggregateAnonymizerRepositoryDecorator<>(decoratedRepo,
				aggregateAnonymizer, appSettings);
	}

	@Test
	public void delegate() throws Exception
	{
		assertEquals(aggregateAnonymizerRepoDecorator.delegate(), decoratedRepo);
	}

	@Test
	public void aggregateNoThreshold() throws Exception
	{
		when(appSettings.getAggregateThreshold()).thenReturn(null);
		AggregateQuery aggregateQuery = mock(AggregateQuery.class);
		AggregateResult aggregateResult = mock(AggregateResult.class);
		when(decoratedRepo.aggregate(aggregateQuery)).thenReturn(aggregateResult);
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
		when(decoratedRepo.aggregate(aggregateQuery)).thenReturn(aggregateResult);
		AnonymizedAggregateResult anonymizedAggregateResult = mock(AnonymizedAggregateResult.class);
		when(aggregateAnonymizer.anonymize(aggregateResult, threshold)).thenReturn(anonymizedAggregateResult);
		assertEquals(anonymizedAggregateResult, aggregateAnonymizerRepoDecorator.aggregate(aggregateQuery));
	}
}