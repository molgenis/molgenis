package org.molgenis.data;

import static org.molgenis.util.SecurityDecoratorUtils.validatePermission;

import org.molgenis.security.core.Permission;

public class AggregateableCrudRepositorySecurityDecorator extends CrudRepositoryDecorator implements Aggregateable
{
	private final Aggregateable aggregateableRepository;

	public AggregateableCrudRepositorySecurityDecorator(CrudRepository decoratedRepository)
	{
		super(decoratedRepository);

		if (!(decoratedRepository instanceof Aggregateable))
		{
			throw new IllegalArgumentException("Repository does not implement Aggregateable");
		}

		this.aggregateableRepository = (Aggregateable) decoratedRepository;
	}

	@Override
	public AggregateResult aggregate(AttributeMetaData xAttr, AttributeMetaData yAttr, Query q)
	{
		validatePermission(getName(), Permission.COUNT);
		return aggregateableRepository.aggregate(xAttr, yAttr, q);
	}

}
