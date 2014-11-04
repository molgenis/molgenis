package org.molgenis.data.support;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.springframework.transaction.annotation.Transactional;

public abstract class AbstractCrudRepository extends AbstractRepository implements CrudRepository
{
	public AbstractCrudRepository(String url)
	{
		super(url);
	}

	@Override
	public Query query()
	{
		return new QueryImpl(this);
	}

	@Override
	@Transactional(readOnly = true)
	public <E extends Entity> Iterable<E> findAll(Query q, Class<E> clazz)
	{
		return new ConvertingIterable<E>(clazz, findAll(q));
	}
}
