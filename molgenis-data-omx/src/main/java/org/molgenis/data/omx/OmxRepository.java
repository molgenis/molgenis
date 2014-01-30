package org.molgenis.data.omx;

import java.util.Iterator;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Queryable;
import org.molgenis.data.support.ConvertingIterable;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.dataset.AbstractDataSetMatrixRepository;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.search.SearchService;
import org.springframework.beans.BeanUtils;

/**
 * Repository around an omx DataSet matrix.
 * 
 * Uses the DataService to get the metadata and the SearchService to get the actual data itself
 */
public class OmxRepository extends AbstractDataSetMatrixRepository implements Queryable
{
	private final SearchService searchService;

	public OmxRepository(DataService dataService, SearchService searchService, String dataSetIdentifier)
	{
		super(dataService, dataSetIdentifier);
		this.searchService = searchService;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return new OmxRepositoryIterator(dataSetIdentifier, searchService, new QueryImpl(), getAttributeNames());
	}

	@Override
	public long count()
	{
		return count(new QueryImpl());
	}

	@Override
	public long count(Query q)
	{
		return searchService.count(dataSetIdentifier, q);
	}

	@Override
	public Iterable<Entity> findAll(final Query q)
	{
		return new Iterable<Entity>()
		{
			@Override
			public Iterator<Entity> iterator()
			{
				return new OmxRepositoryIterator(dataSetIdentifier, searchService, q, getAttributeNames());
			}
		};
	}

	@Override
	public Entity findOne(Query q)
	{
		q.pageSize(1);
		Iterator<Entity> it = findAll(q).iterator();
		if (!it.hasNext())
		{
			return null;
		}

		return it.next();
	}

	@Override
	public Entity findOne(Integer id)
	{
		Query q = new QueryImpl().eq(ObservationSet.ID, id);
		return findOne(q);
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Integer> ids)
	{
		Query q = new QueryImpl().in(ObservationSet.ID, ids);
		return findAll(q);
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Query q, Class<E> clazz)
	{
		return new ConvertingIterable<E>(clazz, findAll(q));
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Iterable<Integer> ids, Class<E> clazz)
	{
		return new ConvertingIterable<E>(clazz, findAll(ids));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> E findOne(Integer id, Class<E> clazz)
	{
		Entity entity = findOne(id);
		if (entity == null)
		{
			return null;
		}

		if (clazz.isAssignableFrom(entity.getClass()))
		{
			return (E) entity;
		}

		E e = BeanUtils.instantiate(clazz);
		e.set(entity);
		return e;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> E findOne(Query q, Class<E> clazz)
	{
		Entity entity = findOne(q);
		if (entity == null)
		{
			return null;
		}

		if (clazz.isAssignableFrom(entity.getClass()))
		{
			return (E) entity;
		}

		E e = BeanUtils.instantiate(clazz);
		e.set(entity);
		return e;
	}

}
