package org.molgenis.data.security.owned;

import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.security.EntityIdentity;
import org.molgenis.data.security.EntityPermission;
import org.molgenis.data.security.EntityPermissionUtils;
import org.molgenis.data.security.EntityTypePermission;
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
import static org.molgenis.data.security.EntityTypePermission.READ;
import static org.molgenis.data.security.EntityTypePermission.WRITE;

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
	public Query<Entity> query()
	{
		return new QueryImpl<>(this);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return findAll(new QueryImpl<>()).filter(entity -> hasPermissionOnEntity(entity, READ)).iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		delegate().forEachBatched(fetch, entities ->
		{
			//TODO: This results in smaller batches! Should do a findAll instead!
			consumer.accept(entities.stream().filter(entity -> hasPermissionOnEntity(entity, READ)).collect(toList()));
		}, batchSize);
	}

	@Override
	public long count()
	{
		return count(new QueryImpl<>());
	}

	@Override
	public long count(Query<Entity> q)
	{
		return findAll(q).collect(toList()).size();
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		return delegate().findAll(q).filter(entity -> hasPermissionOnEntity(entity, READ));
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
			delegate().delete(entity);
		}
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		entities = entities.filter(entity -> hasPermissionOnEntity(entity, WRITE));

		delegate().delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		if (hasPermissionOnEntity(id, WRITE))
		{
			delegate().deleteById(id);
		}
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		ids = ids.filter(entity -> hasPermissionOnEntity(entity, WRITE));
		delegate().deleteAll(ids);
	}

	@Override
	public void deleteAll()
	{
		delegate().forEachBatched(entities -> delete(entities.stream()), 1000);
	}

	@Override
	public void add(Entity entity)
	{
		// TODO decide whether we want to write ACEs for superusers, considering the following use case:
		// as user #0 with superuser=true access import dataset
		// as user #1 with superuser=true access remove set superuser=false for user #0
		// can user #0 still access the imported data? (if an ace exists --> yes, otherwise no)

		MutableAcl acl = mutableAclService.createAcl(new EntityIdentity(entity));
		Sid sid = new PrincipalSid(SecurityUtils.getCurrentUsername());
		acl.insertAce(acl.getEntries().size(), EntityPermissionUtils.getCumulativePermission(EntityPermission.WRITE),
				sid, true);
		mutableAclService.updateAcl(acl);

		delegate().add(entity);
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		return delegate().add(entities.filter(entity ->
		{
			mutableAclService.createAcl(new EntityIdentity(entity));
			return true;
		}));
	}

	private boolean hasPermissionOnEntity(Entity entity, EntityTypePermission permission)
	{
		EntityIdentity entityIdentity = new EntityIdentity(entity);
		return hasPermissionOnEntity(entityIdentity, permission);
	}

	private boolean hasPermissionOnEntity(Object id, EntityTypePermission permission)
	{
		EntityIdentity entityIdentity = new EntityIdentity(getEntityType().getId(), id);
		return hasPermissionOnEntity(entityIdentity, permission);
	}

	private boolean hasPermissionOnEntity(EntityIdentity entityIdentity, EntityTypePermission permission)
	{
		return userPermissionEvaluator.hasPermission(entityIdentity, permission);
	}
}
