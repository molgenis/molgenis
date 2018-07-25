package org.molgenis.data.decorator;

import org.molgenis.data.*;
import org.molgenis.data.support.QueryImpl;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

/**
 * Decorator that checks if actions on this repository are allowed using a PermissionChecker.
 *
 * @param <E> the class of the entities in the repository
 */
public class PermissionCheckingDecorator<E extends Entity> extends AbstractRepositoryDecorator<E>
{
	private PermissionChecker<E> permissionChecker;

	public PermissionCheckingDecorator(Repository<E> delegateRepository, PermissionChecker<E> permissionChecker)
	{
		super(delegateRepository);
		this.permissionChecker = requireNonNull(permissionChecker);
	}

	@Override
	public Iterator<E> iterator()
	{
		Iterable<E> iterable = () -> delegate().iterator();
		return stream(iterable.spliterator(), false).filter(
				entity -> permissionChecker.isReadAllowed(entity.getIdValue())).iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<E>> consumer, int batchSize)
	{
		delegate().forEachBatched(fetch, entities -> consumer.accept(entities.stream()
																			 .filter(entity -> permissionChecker.isReadAllowed(
																					 entity.getIdValue()))
																			 .collect(toList())), batchSize);
	}

	@Override
	public long count()
	{
		return count(new QueryImpl<>());
	}

	@Override
	public long count(Query<E> q)
	{
		return findAll(q).count();
	}

	@Override
	public Stream<E> findAll(Query<E> q)
	{
		Query<E> qWithoutLimitOffset = new QueryImpl<>(q);
		qWithoutLimitOffset.offset(0).pageSize(Integer.MAX_VALUE);
		Stream<E> permittedEntityStream = delegate().findAll(qWithoutLimitOffset)
													.filter(e -> permissionChecker.isReadAllowed(e.getIdValue()));
		if (q.getOffset() > 0)
		{
			permittedEntityStream = permittedEntityStream.skip(q.getOffset());
		}
		if (q.getPageSize() > 0)
		{
			permittedEntityStream = permittedEntityStream.limit(q.getPageSize());
		}
		return permittedEntityStream;
	}

	@Override
	public E findOne(Query<E> q)
	{
		return findAll(q).findFirst().orElse(null);
	}

	@Override
	public E findOneById(Object id)
	{
		E entity = null;
		if (permissionChecker.isReadAllowed(id))
		{
			entity = delegate().findOneById(id);
		}
		return entity;
	}

	@Override
	public E findOneById(Object id, Fetch fetch)
	{
		E entity = null;
		if (permissionChecker.isReadAllowed(id))
		{
			entity = delegate().findOneById(id, fetch);
		}
		return entity;
	}

	@Override
	public Stream<E> findAll(Stream<Object> ids)
	{
		return delegate().findAll(ids.filter(permissionChecker::isReadAllowed));
	}

	@Override
	public Stream<E> findAll(Stream<Object> ids, Fetch fetch)
	{
		return delegate().findAll(ids.filter(permissionChecker::isReadAllowed), fetch);
	}

	@Override
	public void update(E entity)
	{
		if (permissionChecker.isUpdateAllowed(entity))
		{
			delegate().update(entity);
		}
	}

	@Override
	public void update(Stream<E> entities)
	{
		delegate().update(entities.filter(e -> permissionChecker.isUpdateAllowed(e.getIdValue())));
	}

	@Override
	public void delete(E entity)
	{
		if (permissionChecker.isDeleteAllowed(entity.getIdValue()))
		{
			delegate().delete(entity);
		}
	}

	@Override
	public void delete(Stream<E> entities)
	{
		deleteStream(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		if (permissionChecker.isDeleteAllowed(id))
		{
			delegate().deleteById(id);
		}
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		delegate().deleteAll(ids.filter(permissionChecker::isDeleteAllowed));
	}

	@Override
	public void deleteAll()
	{
		deleteAll(delegate().findAll(new QueryImpl<>()).map(Entity::getIdValue));
	}

	@Override
	public void add(E entity)
	{
		if (permissionChecker.isAddAllowed(entity))
		{
			delegate().add(entity);
		}
	}

	@Override
	public Integer add(Stream<E> entities)
	{
		return delegate().add(entities.filter(permissionChecker::isAddAllowed));
	}

	private void deleteStream(Stream<E> entityStream)
	{
		delegate().deleteAll(entityStream.map(Entity::getIdValue).filter(permissionChecker::isDeleteAllowed));
	}

}
