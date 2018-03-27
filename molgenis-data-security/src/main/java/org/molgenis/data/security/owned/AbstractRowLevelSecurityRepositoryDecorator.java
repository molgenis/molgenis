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

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSuOrSystem;

/**
 * RepositoryDecorator that works on EntityTypes that are row-level secured.
 */
public abstract class AbstractRowLevelSecurityRepositoryDecorator<E extends Entity>
		extends AbstractRepositoryDecorator<E>
{
	private final MutableAclService mutableAclService;

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
		return stream(iterable.spliterator(), false).filter(entity -> isOperationPermitted(entity, Action.READ))
													.iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<E>> consumer, int batchSize)
	{
		delegate().forEachBatched(fetch, entities -> consumer.accept(
				entities.stream().filter(entity -> isOperationPermitted(entity, Action.READ)).collect(toList())),
				batchSize);
	}

	@Override
	public long count()
	{
		return findAllPermitted(Action.COUNT).count();
	}

	@Override
	public long count(Query<E> q)
	{
		return findAllPermitted(q, Action.COUNT).count();
	}

	@Override
	public Stream<E> findAll(Query<E> q)
	{
		return findAllPermitted(q, Action.READ);
	}

	@Override
	public E findOne(Query<E> q)
	{
		E entity = delegate().findOne(q);

		if (entity != null && !isOperationPermitted(entity, Action.READ))
		{
			return null;
		}

		return entity;
	}

	@Override
	public E findOneById(Object id)
	{
		E entity = null;

		if (isOperationPermitted(id, Action.READ))
		{
			entity = delegate().findOneById(id);
		}
		return entity;
	}

	@Override
	public E findOneById(Object id, Fetch fetch)
	{
		E entity = null;
		if (isOperationPermitted(id, Action.READ))
		{
			entity = delegate().findOneById(id, fetch);
		}
		return entity;
	}

	@Override
	public Stream<E> findAll(Stream<Object> ids)
	{
		Stream<E> entities = delegate().findAll(ids);
		entities = entities.filter(entity -> isOperationPermitted(entity, Action.READ));
		return entities;
	}

	@Override
	public Stream<E> findAll(Stream<Object> ids, Fetch fetch)
	{
		return delegate().findAll(ids, fetch).filter(entity -> isOperationPermitted(entity, Action.READ));
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
		if (!isOperationPermitted(entity, Action.UPDATE))
		{
			throw new MolgenisDataAccessException(
					format("No [%s] permission on entity type [%s] with id [%s]", toMessagePermission(Action.DELETE),
							entity.getEntityType().getLabel(), entity.getIdValue()));
		}
		delegate().update(entity);
		updateAcl(entity);
	}

	@Override
	public void update(Stream<E> entities)
	{
		delegate().update(entities.filter((E entity) ->
		{
			boolean result = isOperationPermitted(entity.getIdValue(), Action.UPDATE);
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
		if (!isOperationPermitted(entity, Action.DELETE))
		{
			throw new MolgenisDataAccessException(
					format("No [%s] permission on entity type [%s] with id [%s]", toMessagePermission(Action.DELETE),
							entity.getEntityType().getLabel(), entity.getIdValue()));
		}
		deleteAcl(entity);
		delegate().delete(entity);
	}

	protected abstract String toMessagePermission(Action action);

	@Override
	public void delete(Stream<E> entities)
	{
		deleteStream(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		if (isOperationPermitted(id, Action.DELETE))
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
			boolean deleteAllowed = isOperationPermitted(id, Action.DELETE);
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
		delegate().delete(findAllPermitted(new QueryImpl<>(), Action.DELETE));
	}

	@Override
	public void add(E entity)
	{
		createAcl(entity);
		delegate().add(entity);
	}

	@Override
	public Integer add(Stream<E> entities)
	{
		return delegate().add(entities.filter(entity ->
		{
			createAcl(entity);
			return true;
		}));
	}

	private void deleteStream(Stream<E> entityStream)
	{
		delegate().delete(entityStream.filter(entity ->
		{
			boolean deleteAllowed = isOperationPermitted(entity, Action.DELETE);
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
													.filter(entity -> isOperationPermitted(entity, action));
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

	public void deleteAcl(ObjectIdentity objectIdentity)
	{
		mutableAclService.deleteAcl(objectIdentity, true);
	}

	public abstract boolean isOperationPermitted(E entity, Action action);

	public abstract boolean isOperationPermitted(Object id, Action action);

	public abstract void createAcl(E entity);

	public abstract void deleteAcl(E entity);

	public abstract void deleteAcl(Object id);

	public abstract void updateAcl(E entity);
}
