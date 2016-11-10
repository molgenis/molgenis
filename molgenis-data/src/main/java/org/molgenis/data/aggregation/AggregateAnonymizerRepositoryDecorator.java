package org.molgenis.data.aggregation;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.settings.AppSettings;

import static java.util.Objects.requireNonNull;

/**
 * Repository decorator that anonymizes aggregate query results based on application anonymization settings.
 */
public class AggregateAnonymizerRepositoryDecorator<E extends Entity> extends AbstractRepositoryDecorator<E>
{
	private final Repository<E> decoratedRepo;
	private final AggregateAnonymizer aggregateAnonymizer;
	private final AppSettings appSettings;

	public AggregateAnonymizerRepositoryDecorator(Repository<E> decoratedRepo, AggregateAnonymizer aggregateAnonymizer,
			AppSettings appSettings)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
		this.appSettings = requireNonNull(appSettings);
		this.aggregateAnonymizer = requireNonNull(aggregateAnonymizer);
	}

	@Override
	protected Repository<E> delegate()
	{
		return decoratedRepo;
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		AggregateResult result = decoratedRepo.aggregate(aggregateQuery);

		Integer threshold = appSettings.getAggregateThreshold();
		if (threshold != null && threshold > 0)
		{
			result = aggregateAnonymizer.anonymize(result, threshold);
		}

		return result;
	}
}
