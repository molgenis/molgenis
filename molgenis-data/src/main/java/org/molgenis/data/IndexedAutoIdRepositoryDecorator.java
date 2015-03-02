package org.molgenis.data;

public class IndexedAutoIdRepositoryDecorator extends AutoIdCrudRepositoryDecorator implements IndexedCrudRepository
{
	public IndexedAutoIdRepositoryDecorator(IndexedCrudRepository decoratedRepository, IdGenerator idGenerator)
	{
		super(decoratedRepository, idGenerator);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return ((IndexedCrudRepository) getDecoratedRepository()).aggregate(aggregateQuery);
	}

	@Override
	public void create()
	{
		((IndexedCrudRepository) getDecoratedRepository()).create();
	}

	@Override
	public void drop()
	{
		((IndexedCrudRepository) getDecoratedRepository()).drop();

	}

	@Override
	public void rebuildIndex()
	{
		((IndexedCrudRepository) getDecoratedRepository()).rebuildIndex();
	}

}
