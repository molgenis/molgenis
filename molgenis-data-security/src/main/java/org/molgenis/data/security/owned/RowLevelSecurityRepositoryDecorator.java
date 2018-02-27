package org.molgenis.data.security.owned;

import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.security.EntityIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.acl.SidUtils;
import org.molgenis.data.security.meta.RowLevelSecuredMetadata;
import org.molgenis.data.security.user.UserService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.core.runas.RunAsSystemAspect;
import org.molgenis.security.core.utils.SecurityUtils;
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
	private final DataService dataService;
	private final UserPermissionEvaluator userPermissionEvaluator;
	private final MutableAclService mutableAclService;
	private final UserService userService;

	public RowLevelSecurityRepositoryDecorator(Repository<Entity> delegateRepository, DataService dataService,
			UserPermissionEvaluator userPermissionEvaluator, MutableAclService mutableAclService,
			UserService userService)
	{
		super(delegateRepository);
		this.dataService = requireNonNull(dataService);
		this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
		this.mutableAclService = mutableAclService;
		this.userService = userService;
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

		if (isRowLevelSecured() && !hasPermissionOnEntity(entity, READ))
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
			mutableAclService.createAcl(new EntityIdentity(getEntityType(), entity));
		}
		delegate().add(entity);
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		return delegate().add(entities.filter(entity ->
		{
			mutableAclService.createAcl(new EntityIdentity(getEntityType(), entity));
			return true;
		}));
	}

	private boolean isRowLevelSecured()
	{
		if (this.getName().equals(RowLevelSecuredMetadata.ROW_LEVEL_SECURED))
		{
			return false;
		}
		return RunAsSystemAspect.runAsSystem(() -> dataService.findOne(RowLevelSecuredMetadata.ROW_LEVEL_SECURED,
				new QueryImpl<>().eq(RowLevelSecuredMetadata.ENTITYTYPE_ID, delegate().getName()))) != null;
	}

	private boolean hasPermissionOnEntity(Entity entity, EntityTypePermission permission)
	{
		return hasPermissionOnEntity(entity.getIdValue(), permission);
	}

	private boolean hasPermissionOnEntity(Object id, EntityTypePermission permission)
	{
		Sid owner = SidUtils.createSid(userService.getUser(SecurityUtils.getCurrentUsername()));
		EntityIdentity entityIdentity = new EntityIdentity(getEntityType().getId(), id);

		return mutableAclService.readAclById(entityIdentity).getOwner().equals(owner)
				|| userPermissionEvaluator.hasPermission(entityIdentity, permission);
	}
}
