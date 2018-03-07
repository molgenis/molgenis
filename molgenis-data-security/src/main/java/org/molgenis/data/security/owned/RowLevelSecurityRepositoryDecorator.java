package org.molgenis.data.security.owned;

import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.security.EntityIdentity;
import org.molgenis.data.security.EntityPermission;
import org.molgenis.data.security.EntityPermissionUtils;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.Sid;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.security.EntityPermission.READ;
import static org.molgenis.data.security.EntityPermission.WRITE;

/**
 * RepositoryDecorator that works on EntityType that extends OwnedEntityType.
 * <p>
 * Ensures that when an Entity is created the owner is set to the current user, users can only view, update, delete
 * their own entities.
 * <p>
 * Admins are not effected.
 */
public class RowLevelSecurityRepositoryDecorator extends AbstractRepositoryDecorator<Entity>
{
	private final UserPermissionEvaluator userPermissionEvaluator;
	private final MutableAclService mutableAclService;

	RowLevelSecurityRepositoryDecorator(Repository<Entity> delegateRepository,
			UserPermissionEvaluator userPermissionEvaluator, MutableAclService mutableAclService)
	{
		super(delegateRepository);
		this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
		this.mutableAclService = requireNonNull(mutableAclService);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		Iterable<Entity> iterable = () -> delegate().iterator();
		return stream(iterable.spliterator(), false).filter(entity -> hasPermissionOnEntity(entity, READ)).iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		delegate().forEachBatched(fetch, entities -> consumer.accept(
				entities.stream().filter(entity -> hasPermissionOnEntity(entity, READ)).collect(toList())), batchSize);
	}

	@Override
	public long count()
	{
		return findAllPermittedWithoutLimitOffset(EntityPermission.COUNT).count();
	}

	@Override
	public long count(Query<Entity> q)
	{
		return findAllPermittedWithoutLimitOffset(q, EntityPermission.COUNT).count();
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		return findAllPermitted(q, EntityPermission.READ);
	}

	@Override
	public Entity findOne(Query<Entity> q)
	{
		Entity entity = delegate().findOne(q);

		if (entity != null && !hasPermissionOnEntity(entity, READ))
		{
			return null;
		}

		return entity;
	}

	@Override
	public Entity findOneById(Object id)
	{
		Entity entity = null;

		if (hasPermissionOnEntity(id, READ))
		{
			entity = delegate().findOneById(id);
		}

		return entity;
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		Entity entity = null;
		if (hasPermissionOnEntity(id, READ))
		{
			entity = delegate().findOneById(id, fetch);
		}
		return entity;
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		Stream<Entity> entities = delegate().findAll(ids);
		entities = entities.filter(entity -> hasPermissionOnEntity(entity, READ));
		return entities;
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		return delegate().findAll(ids, fetch).filter(entity -> hasPermissionOnEntity(entity, READ));
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		if (!SecurityUtils.currentUserIsSuOrSystem())
		{
			throw new UnsupportedOperationException();
		}
		return delegate().aggregate(aggregateQuery);
	}

	@Override
	public void update(Entity entity)
	{
		if (hasPermissionOnEntity(entity, WRITE))
		{
			delegate().update(entity);
		}
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		delegate().update(entities.filter(entity -> hasPermissionOnEntity(entity, WRITE)));
	}

	@Override
	public void delete(Entity entity)
	{
		if (hasPermissionOnEntity(entity, WRITE))
		{
			deleteAcl(entity);
			delegate().delete(entity);
		}
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		deleteStream(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		if (hasPermissionOnEntity(id, WRITE))
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
			boolean deleteAllowed = hasPermissionOnEntity(id, WRITE);
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
		delegate().delete(findAllPermitted(new QueryImpl<>(), EntityPermission.WRITE));
	}

	@Override
	public void add(Entity entity)
	{
		createAcl(entity);
		delegate().add(entity);
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		return delegate().add(entities.filter(entity ->
		{
			createAcl(entity);
			return true;
		}));
	}

	private boolean hasPermissionOnEntity(Entity entity, EntityPermission permission)
	{
		EntityIdentity entityIdentity = new EntityIdentity(entity);
		return hasPermissionOnEntity(entityIdentity, permission);
	}

	private boolean hasPermissionOnEntity(Object id, EntityPermission permission)
	{
		EntityIdentity entityIdentity = toEntityIdentity(id);
		return hasPermissionOnEntity(entityIdentity, permission);
	}

	private boolean hasPermissionOnEntity(EntityIdentity entityIdentity, EntityPermission permission)
	{
		return userPermissionEvaluator.hasPermission(entityIdentity, permission);
	}

	private void createAcl(Entity entity)
	{
		MutableAcl acl = mutableAclService.createAcl(new EntityIdentity(entity));
		Sid sid = new PrincipalSid(SecurityUtils.getCurrentUsername());
		acl.insertAce(acl.getEntries().size(), EntityPermissionUtils.getCumulativePermission(WRITE), sid, true);
		mutableAclService.updateAcl(acl);
	}

	private void deleteAcl(Entity entity)
	{
		EntityIdentity entityIdentity = new EntityIdentity(entity);
		deleteAcl(entityIdentity);
	}

	private void deleteAcl(Object id)
	{
		EntityIdentity entityIdentity = toEntityIdentity(id);
		deleteAcl(entityIdentity);
	}

	private void deleteAcl(EntityIdentity entityIdentity)
	{
		mutableAclService.deleteAcl(entityIdentity, true);
	}

	private EntityIdentity toEntityIdentity(Object entityId)
	{
		return new EntityIdentity(getEntityType().getId(), entityId);
	}

	private void deleteStream(Stream<Entity> entityStream)
	{
		delegate().delete(entityStream.filter(entity ->
		{
			boolean deleteAllowed = hasPermissionOnEntity(entity, WRITE);
			if (deleteAllowed)
			{
				deleteAcl(entity);
			}
			return deleteAllowed;
		}));
	}

	private Stream<Entity> findAllPermitted(Query<Entity> query, EntityPermission entityPermission)
	{
		Stream<Entity> permittedEntityStream = findAllPermittedWithoutLimitOffset(query, entityPermission);
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

	private Stream<Entity> findAllPermittedWithoutLimitOffset(Query<Entity> query, EntityPermission entityPermission)
	{
		Query<Entity> qWithoutLimitOffset = new QueryImpl<>(query);
		qWithoutLimitOffset.offset(0).pageSize(Integer.MAX_VALUE);
		return delegate().findAll(qWithoutLimitOffset)
						 .filter(entity -> hasPermissionOnEntity(entity, entityPermission));
	}

	private Stream<Entity> findAllPermittedWithoutLimitOffset(EntityPermission entityPermission)
	{
		return findAllPermitted(new QueryImpl<>(), entityPermission);
	}
}
