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
	public void rebuildIndex()
	{
		decoratedRepository.rebuildIndex();
	}
}
