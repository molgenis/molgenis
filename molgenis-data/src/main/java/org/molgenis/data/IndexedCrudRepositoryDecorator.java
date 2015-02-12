package org.molgenis.data;

public class IndexedCrudRepositoryDecorator extends CrudRepositoryDecorator implements IndexedRepository
{
	private final IndexedCrudRepository decoratedRepository;

	public IndexedCrudRepositoryDecorator(IndexedCrudRepository decoratedRepository)
	{
		super(decoratedRepository);
		this.decoratedRepository = decoratedRepository;
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return decoratedRepository.aggregate(aggregateQuery);
	}

	@Override
	public void rebuildIndex()
	{
		decoratedRepository.rebuildIndex();
	}

	@Override
	public void create()
	{
		decoratedRepository.create();
	}

	@Override
	public void drop()
	{
		decoratedRepository.drop();
	}
}
