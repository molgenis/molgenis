package org.molgenis.data;

import static org.molgenis.util.SecurityDecoratorUtils.validatePermission;

import org.molgenis.data.support.AggregateAnonymizerImpl;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.security.core.Permission;

public class IndexedCrudRepositorySecurityDecorator extends CrudRepositorySecurityDecorator implements
		IndexedRepository
{
	public static final String SETTINGS_KEY_AGGREGATE_ANONYMIZATION_THRESHOLD = "aggregate.anonymization.threshold";
	private final IndexedCrudRepository decoratedRepository;
	private final MolgenisSettings molgenisSettings;
	private final AggregateAnonymizer aggregateAnonymizer = new AggregateAnonymizerImpl();

	public IndexedCrudRepositorySecurityDecorator(IndexedCrudRepository decoratedRepository,
			MolgenisSettings molgenisSettings)
	{
		super(decoratedRepository);
		this.decoratedRepository = decoratedRepository;
		this.molgenisSettings = molgenisSettings;
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		validatePermission(decoratedRepository.getName(), Permission.COUNT);

		Integer threshold = molgenisSettings.getIntegerProperty(SETTINGS_KEY_AGGREGATE_ANONYMIZATION_THRESHOLD);

		AggregateResult result = decoratedRepository.aggregate(aggregateQuery);
		if (threshold != null && threshold > 0)
		{
			result = aggregateAnonymizer.anonymize(result, threshold);
		}
		return result;
	}

	@Override
	public void rebuildIndex()
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.rebuildIndex();
	}

	@Override
	public void drop()
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.drop();
	}
}
