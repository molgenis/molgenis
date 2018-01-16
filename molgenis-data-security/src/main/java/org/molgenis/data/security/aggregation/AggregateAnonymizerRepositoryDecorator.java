package org.molgenis.data.security.aggregation;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.settings.AppSettings;

import static java.util.Objects.requireNonNull;

/**
 * Repository decorator that anonymizes aggregate query results based on application anonymization settings.
 */
public class AggregateAnonymizerRepositoryDecorator<E extends Entity> extends AbstractRepositoryDecorator<E>
{
	private final AggregateAnonymizer aggregateAnonymizer;
	private final AppSettings appSettings;

	public AggregateAnonymizerRepositoryDecorator(Repository<E> delegateRepository,
			AggregateAnonymizer aggregateAnonymizer, AppSettings appSettings)
	{
		super(delegateRepository);
		this.appSettings = requireNonNull(appSettings);
		this.aggregateAnonymizer = requireNonNull(aggregateAnonymizer);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		AggregateResult result = delegate().aggregate(aggregateQuery);

		Integer threshold = appSettings.getAggregateThreshold();
		if (threshold != null && threshold > 0)
		{
			result = aggregateAnonymizer.anonymize(result, threshold);
		}

		return result;
	}
}
