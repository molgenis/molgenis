package org.molgenis.data;

public class IndexedAutoIdRepositoryDecorator extends AutoIdRepositoryDecorator implements IndexedRepository
{
	private final IndexedRepository indexedRepository;

	public IndexedAutoIdRepositoryDecorator(IndexedRepository indexedRepository, IdGenerator idGenerator)
	{
		super(indexedRepository, idGenerator);
		this.indexedRepository = indexedRepository;
	}

	@Override
	public void create()
	{
		indexedRepository.create();
	}

	@Override
	public void drop()
	{
		indexedRepository.drop();

	}

	@Override
	public void rebuildIndex()
	{
		indexedRepository.rebuildIndex();
	}

}
