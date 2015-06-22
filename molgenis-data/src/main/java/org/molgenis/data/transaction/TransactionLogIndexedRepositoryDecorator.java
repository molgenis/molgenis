package org.molgenis.data.transaction;

import org.molgenis.data.IndexedRepository;

public class TransactionLogIndexedRepositoryDecorator extends TransactionLogRepositoryDecorator implements
		IndexedRepository
{
	private IndexedRepository decorated;

	public TransactionLogIndexedRepositoryDecorator(IndexedRepository decorated,
			TransactionLogService transactionLogService)
	{
		super(decorated, transactionLogService);
		this.decorated = decorated;
	}

	@Override
	public void create()
	{
		decorated.create();
	}

	@Override
	public void drop()
	{
		decorated.drop();
	}

	@Override
	public void rebuildIndex()
	{
		decorated.rebuildIndex();
	}

}
