package org.molgenis.data.security.owned;

import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.security.*;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.acl.MutableAclClassService;
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
	private final MutableAclClassService mutableAclClassService;

	public RowLevelSecurityRepositoryDecorator(Repository<Entity> delegateRepository,
			UserPermissionEvaluator userPermissionEvaluator, MutableAclService mutableAclService,
			MutableAclClassService mutableAclClassService)
	{
		super(delegateRepository);
		this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
		this.mutableAclService = requireNonNull(mutableAclService);
		this.mutableAclClassService = requireNonNull(mutableAclClassService);
	}

	@Override
	public Query<Entity> query()
	{
		return new QueryImpl<>(this);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		if (isRowLevelSecured())
			return findAll(new QueryImpl<>()).filter(entity -> hasPermissionOnEntity(entity, READ)).iterator();
		return delegate().iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		delegate().forEachBatched(fetch, entities ->
		{
			if (isRowLevelSecured())
			{
				//TODO: This results in smaller batches! Should do a findAll instead!
				consumer.accept(
						entities.stream().filter(entity -> hasPermissionOnEntity(entity, READ)).collect(toList()));
			}
			else
			{
				consumer.accept(entities);
			}
		}, batchSize);
	}

	@Override
	public long count()
	{
		if (isRowLevelSecured())
		{
			return count(new QueryImpl<>());
		}
		return delegate().count();
	}

	@Override
	public long count(Query<Entity> q)
	{
		if (isRowLevelSecured())
		{
			return findAll(q).collect(toList()).size();
		}
		return delegate().count(q);
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		if (isRowLevelSecured())
		{
			return delegate().findAll(q).filter(entity -> hasPermissionOnEntity(entity, READ));
		}
		return delegate().findAll(q);
	}

	@Override
	public Entity findOne(Query<Entity> q)
	{
		Entity entity = delegate().findOne(q);

		if (entity != null && isRowLevelSecured() && !hasPermissionOnEntity(entity, READ))
		{
			return null;
		}

		return entity;
	}

	@Override
	public Entity findOneById(Object id)
	{
		Entity entity = null;

		if (!isRowLevelSecured() || hasPermissionOnEntity(id, READ))
		{
			entity = delegate().findOneById(id);
		}

		return entity;
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		Entity entity = null;
		if (!isRowLevelSecured() || hasPermissionOnEntity(id, READ))
		{
			entity = delegate().findOneById(id, fetch);
		}
		return entity;
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		Stream<Entity> entities = delegate().findAll(ids);
		if (isRowLevelSecured())
		{
			entities = entities.filter(entity -> hasPermissionOnEntity(entity, READ));
		}
		return entities;
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		if (isRowLevelSecured())
		{
			return delegate().findAll(ids, fetch).filter(entity -> hasPermissionOnEntity(entity, READ));
		}
		return delegate().findAll(ids, fetch);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		if (isRowLevelSecured())
		{
			if (!SecurityUtils.currentUserIsSuOrSystem())
			{
				throw new UnsupportedOperationException();
			}
		}

		return delegate().aggregate(aggregateQuery);
	}

	@Override
	public void update(Entity entity)
	{
		if (!isRowLevelSecured() || hasPermissionOnEntity(entity, WRITE))
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
		if (isRowLevelSecured())
		{
			entities = entities.filter(entity -> hasPermissionOnEntity(entity, WRITE));
		}

		delegate().delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		if (!isRowLevelSecured() || hasPermissionOnEntity(id, WRITE))
		{
			delegate().deleteById(id);
		}
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		if (isRowLevelSecured())
		{
			ids = ids.filter(entity -> hasPermissionOnEntity(entity, WRITE));
		}

		delegate().deleteAll(ids);

	}

	@Override
	public void deleteAll()
	{
		if (isRowLevelSecured())
		{
			delegate().forEachBatched(entities -> delete(entities.stream()), 1000);
		}
		else
		{
			delegate().deleteAll();
		}

	}

	@Override
	public void add(Entity entity)
	{
		if (isRowLevelSecured())
		{
			// TODO decide whether we want to write ACEs for superusers, considering the following use case:
			// as user #0 with superuser=true access import dataset
			// as user #1 with superuser=true access remove set superuser=false for user #0
			// can user #0 still access the imported data? (if an ace exists --> yes, otherwise no)

			MutableAcl acl = mutableAclService.createAcl(new EntityIdentity(entity));
			Sid sid = new PrincipalSid(SecurityUtils.getCurrentUsername());
			acl.insertAce(acl.getEntries().size(),
					EntityPermissionUtils.getCumulativePermission(EntityPermission.WRITE), sid, true);
			mutableAclService.updateAcl(acl);
		}
		delegate().add(entity);
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		if (isRowLevelSecured())
		{
			return delegate().add(entities.filter(entity ->
			{
				mutableAclService.createAcl(new EntityIdentity(entity));
				return true;
			}));
		}
		return delegate().add(entities);
	}

	private boolean isRowLevelSecured()
	{
		String aclClass = EntityIdentityUtils.toType(getEntityType());
		return mutableAclClassService.hasAclClass(aclClass);
	}

	private boolean hasPermissionOnEntity(Entity entity, EntityTypePermission permission)
	{
		return hasPermissionOnEntity(entity.getIdValue(), permission);
	}

	private boolean hasPermissionOnEntity(Object id, EntityTypePermission permission)
	{
		EntityIdentity entityIdentity = new EntityIdentity(getEntityType().getId(), id);
		return userPermissionEvaluator.hasPermission(entityIdentity, permission);
	}
}
