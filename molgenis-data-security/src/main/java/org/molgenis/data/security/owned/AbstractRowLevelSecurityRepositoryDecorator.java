package org.molgenis.data.security.owned;

import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.support.QueryImpl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.security.owned.AbstractRowLevelSecurityRepositoryDecorator.Action.*;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSuOrSystem;

/**
 * RepositoryDecorator that works on EntityTypes that are row-level secured.
 */
public abstract class AbstractRowLevelSecurityRepositoryDecorator<E extends Entity>
		extends AbstractRepositoryDecorator<E>
{
	private final MutableAclService mutableAclService;

	/**
	 * The operation that is being performed on this repository.
	 */
	public enum Action
	{
		COUNT, READ, CREATE, UPDATE, DELETE
	}

	public AbstractRowLevelSecurityRepositoryDecorator(Repository<E> delegateRepository,
			MutableAclService mutableAclService)
	{
		super(delegateRepository);
		this.mutableAclService = requireNonNull(mutableAclService);
	}

	@Override
	public Iterator<E> iterator()
	{
		Iterable<E> iterable = () -> delegate().iterator();
		return stream(iterable.spliterator(), false).filter(entity -> isActionPermitted(entity, READ))
													.iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<E>> consumer, int batchSize)
	{
		delegate().forEachBatched(fetch, entities -> consumer.accept(
				entities.stream().filter(entity -> isActionPermitted(entity, READ)).collect(toList())),
				batchSize);
	}

	@Override
	public long count()
	{
		return findAllPermitted(COUNT).count();
	}

	@Override
	public long count(Query<E> q)
	{
		return findAllPermitted(q, COUNT).count();
	}

	@Override
	public Stream<E> findAll(Query<E> q)
	{
		return findAllPermitted(q, READ);
	}

	@Override
	public E findOne(Query<E> q)
	{
		return findAllPermitted(q, READ).findFirst().orElse(null);
	}

	@Override
	public E findOneById(Object id)
	{
		E entity = null;

		if (isActionPermitted(id, READ))
		{
			entity = delegate().findOneById(id);
		}
		return entity;
	}

	@Override
	public E findOneById(Object id, Fetch fetch)
	{
		E entity = null;
		if (isActionPermitted(id, READ))
		{
			entity = delegate().findOneById(id, fetch);
		}
		return entity;
	}

	@Override
	public Stream<E> findAll(Stream<Object> ids)
	{
		Stream<E> entities = delegate().findAll(ids);
		entities = entities.filter(entity -> isActionPermitted(entity, READ));
		return entities;
	}

	@Override
	public Stream<E> findAll(Stream<Object> ids, Fetch fetch)
	{
		return delegate().findAll(ids, fetch).filter(entity -> isActionPermitted(entity, READ));
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		if (!currentUserIsSuOrSystem())
		{
			throw new UnsupportedOperationException();
		}
		return delegate().aggregate(aggregateQuery);
	}

	@Override
	public void update(E entity)
	{
		if (!isActionPermitted(entity, UPDATE))
		{
			throwPermissionException(entity, UPDATE);
		}
		delegate().update(entity);
		updateAcl(entity);
	}

	@Override
	public void update(Stream<E> entities)
	{
		delegate().update(entities.filter((E entity) ->
		{
			boolean result = isActionPermitted(entity, UPDATE);
			if (result)
			{
				updateAcl(entity);
			}
			return result;
		}));
	}

	@Override
	public void delete(E entity)
	{
		if (!isActionPermitted(entity, DELETE))
		{
			throwPermissionException(entity, DELETE);
		}
		deleteAcl(entity);
		delegate().delete(entity);
	}

	@Override
	public void delete(Stream<E> entities)
	{
		deleteStream(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		if (isActionPermitted(id, DELETE))
		{
			deleteAcl(id);
			delegate().deleteById(id);
		}
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		delegate().deleteAll(ids.filter(id ->
		{
			boolean deleteAllowed = isActionPermitted(id, DELETE);
			if (deleteAllowed)
			{
				deleteAcl(id);
			}
			return deleteAllowed;
		}));
	}

	@Override
	public void deleteAll()
	{
		delegate().delete(findAllPermitted(new QueryImpl<>(), DELETE));
	}

	@Override
	public void add(E entity)
	{
		if (isActionPermitted(entity, Action.CREATE))
		{
			createAcl(entity);
			delegate().add(entity);
		}
	}

	@Override
	public Integer add(Stream<E> entities)
	{
		return delegate().add(entities.filter(entity ->
		{
			//throws exception if no permission on the containing package
			isActionPermitted(entity, Action.CREATE);
			createAcl(entity);
			return true;
		}));
	}

	private void deleteStream(Stream<E> entityStream)
	{
		delegate().delete(entityStream.filter(entity ->
		{
			boolean deleteAllowed = isActionPermitted(entity, DELETE);
			if (deleteAllowed)
			{
				deleteAcl(entity);
			}
			return deleteAllowed;
		}));
	}

	private Stream<E> findAllPermitted(Action action)
	{
		return findAllPermitted(new QueryImpl<>(), action);
	}

	private Stream<E> findAllPermitted(Query<E> query, Action action)
	{
		Query<E> qWithoutLimitOffset = new QueryImpl<>(query);
		qWithoutLimitOffset.offset(0).pageSize(Integer.MAX_VALUE);
		Stream<E> permittedEntityStream = delegate().findAll(qWithoutLimitOffset)
													.filter(entity -> isActionPermitted(entity, action));
		if (query.getOffset() > 0)
		{
			permittedEntityStream = permittedEntityStream.skip(query.getOffset());
		}
		if (query.getPageSize() > 0)
		{
			permittedEntityStream = permittedEntityStream.limit(query.getPageSize());
		}
		return permittedEntityStream;
	}

	void deleteAcl(ObjectIdentity objectIdentity)
	{
		mutableAclService.deleteAcl(objectIdentity, true);
	}

	public abstract boolean isActionPermitted(E entity, Action action);

	public abstract boolean isActionPermitted(Object id, Action action);

	public abstract void throwPermissionException(E entity, Action action);

	public abstract void createAcl(E entity);

	public abstract void deleteAcl(E entity);

	public abstract void deleteAcl(Object id);

	public abstract void updateAcl(E entity);
}
