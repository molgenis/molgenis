package org.molgenis.data;

public class IndexedAutoValueRepositoryDecorator extends AutoValueRepositoryDecorator implements IndexedRepository
{
	private final IndexedRepository indexedRepository;

	public IndexedAutoValueRepositoryDecorator(IndexedRepository indexedRepository, IdGenerator idGenerator)
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
