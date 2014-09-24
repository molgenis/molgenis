package org.molgenis.data;

import static org.molgenis.util.SecurityDecoratorUtils.validatePermission;

import org.molgenis.data.support.AggregateQueryImpl;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.security.core.Permission;

public class IndexedCrudRepositorySecurityDecorator extends CrudRepositorySecurityDecorator implements
		IndexedRepository
{
	private static final String SETTINGS_KEY_AGGREGATE_ANONYMIZATION_THRESHOLD = "aggregate.anonymization.threshold";
	private final IndexedCrudRepository decoratedRepository;
	private final MolgenisSettings molgenisSettings;

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
		if (threshold != null)
		{
			if ((aggregateQuery.getAnonymizationThreshold() == null)
					|| (threshold < aggregateQuery.getAnonymizationThreshold()))
			{
				aggregateQuery = new AggregateQueryImpl().anonymizationThreshold(threshold)
						.attrDistinct(aggregateQuery.getAttributeDistinct()).attrX(aggregateQuery.getAttributeX())
						.attrY(aggregateQuery.getAttributeY()).query(aggregateQuery.getQuery());
			}
		}

		return decoratedRepository.aggregate(aggregateQuery);
	}

	@Override
	public void rebuildIndex()
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.rebuildIndex();
	}
}
