package org.molgenis.ui;

import org.molgenis.data.IndexedRepository;

public class EntityListenerIndexedRepositoryDecorator extends EntityListenerRepositoryDecorator
		implements IndexedRepository
{
	private IndexedRepository decoratedRepository;

	public EntityListenerIndexedRepositoryDecorator(IndexedRepository decoratedRepository)
	{
		super(decoratedRepository);
		this.decoratedRepository = decoratedRepository;
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

	@Override
	public void rebuildIndex()
	{
		decoratedRepository.rebuildIndex();
	}
}
