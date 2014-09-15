package org.molgenis.data;

import static org.molgenis.util.SecurityDecoratorUtils.validatePermission;

import org.molgenis.security.core.Permission;

public class IndexedCrudRepositorySecurityDecorator extends CrudRepositorySecurityDecorator implements
		IndexedRepository
{
	private final IndexedCrudRepository decoratedRepository;

	public IndexedCrudRepositorySecurityDecorator(IndexedCrudRepository decoratedRepository)
	{
		super(decoratedRepository);
		this.decoratedRepository = decoratedRepository;
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		validatePermission(decoratedRepository.getName(), Permission.COUNT);
		return decoratedRepository.aggregate(aggregateQuery);
	}

	@Override
	public void rebuildIndex()
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.rebuildIndex();
	}
}
