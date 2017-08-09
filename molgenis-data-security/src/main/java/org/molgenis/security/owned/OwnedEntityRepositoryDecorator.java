package org.molgenis.security.owned;

import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.SystemSecurityToken;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.util.EntityUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.security.owned.OwnedEntityType.OWNED;

/**
 * RepositoryDecorator that works on EntityType that extends OwnedEntityType.
 * <p>
 * Ensures that when an Entity is created the owner is set to the current user, users can only view, update, delete
 * their own entities.
 * <p>
 * Admins are not effected.
 */
public class OwnedEntityRepositoryDecorator extends AbstractRepositoryDecorator<Entity>
{
	private final Repository<Entity> decoratedRepo;

	public OwnedEntityRepositoryDecorator(Repository<Entity> decoratedRepo)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
	}

	@Override
	protected Repository<Entity> delegate()
	{
		return decoratedRepo;
	}

	@Override
	public Query<Entity> query()
	{
		return new QueryImpl<>(this);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		if (mustAddRowLevelSecurity()) return findAll(new QueryImpl<>()).iterator();
		return decoratedRepo.iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		if (fetch != null)
		{
			fetch.field(OwnedEntityType.OWNER_USERNAME);
		}
		decoratedRepo.forEachBatched(fetch, entities ->
		{
			if (mustAddRowLevelSecurity())
			{
				//TODO: This results in smaller batches! Should do a findAll instead!
				consumer.accept(
						entities.stream().filter(OwnedEntityRepositoryDecorator::currentUserIsOwner).collect(toList()));
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
		if (mustAddRowLevelSecurity()) return count(new QueryImpl<>());
		return decoratedRepo.count();
	}

	@Override
	public long count(Query<Entity> q)
	{
		if (mustAddRowLevelSecurity()) addRowLevelSecurity(q);
		return decoratedRepo.count(q);
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		if (mustAddRowLevelSecurity())
		{
			addRowLevelSecurity(q);
		}
		return decoratedRepo.findAll(q);
	}

	@Override
	public Entity findOne(Query<Entity> q)
	{
		if (mustAddRowLevelSecurity()) addRowLevelSecurity(q);
		return decoratedRepo.findOne(q);
	}

	@Override
	public Entity findOneById(Object id)
	{
		Entity e = decoratedRepo.findOneById(id);

		if (mustAddRowLevelSecurity())
		{
			if (!currentUserIsOwner(e)) return null;
		}

		return e;
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		if (fetch != null)
		{
			fetch.field(OwnedEntityType.OWNER_USERNAME);
		}
		Entity e = decoratedRepo.findOneById(id, fetch);

		if (mustAddRowLevelSecurity())
		{
			if (!currentUserIsOwner(e)) return null;
		}

		return e;
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		Stream<Entity> entities = decoratedRepo.findAll(ids);
		if (mustAddRowLevelSecurity())
		{
			entities = entities.filter(OwnedEntityRepositoryDecorator::currentUserIsOwner);
		}
		return entities;
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		if (fetch != null)
		{
			fetch.field(OwnedEntityType.OWNER_USERNAME);
		}
		Stream<Entity> entities = decoratedRepo.findAll(ids, fetch);
		if (mustAddRowLevelSecurity())
		{
			entities = entities.filter(OwnedEntityRepositoryDecorator::currentUserIsOwner);
		}
		return entities;
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		if (mustAddRowLevelSecurity()) addRowLevelSecurity(aggregateQuery.getQuery());
		return decoratedRepo.aggregate(aggregateQuery);
	}

	@Override
	public void update(Entity entity)
	{
		if (isOwnedEntityType() && (mustAddRowLevelSecurity() || entity.get(OwnedEntityType.OWNER_USERNAME) == null))
			entity.set(OwnedEntityType.OWNER_USERNAME, SecurityUtils.getCurrentUsername());
		decoratedRepo.update(entity);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		if (isOwnedEntityType())
		{
			boolean mustAddRowLevelSecurity = mustAddRowLevelSecurity();
			String currentUsername = SecurityUtils.getCurrentUsername();
			entities = entities.map(entity ->
			{
				if (mustAddRowLevelSecurity || entity.get(OwnedEntityType.OWNER_USERNAME) == null)
				{
					entity.set(OwnedEntityType.OWNER_USERNAME, currentUsername);
				}
				return entity;
			});
		}

		decoratedRepo.update(entities);
	}

	@Override
	public void delete(Entity entity)
	{
		if (mustAddRowLevelSecurity() && !currentUserIsOwner(entity)) return;
		decoratedRepo.delete(entity);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		if (mustAddRowLevelSecurity())
		{
			entities = entities.filter(OwnedEntityRepositoryDecorator::currentUserIsOwner);
		}

		decoratedRepo.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		if (mustAddRowLevelSecurity())
		{
			Entity entity = findOneById(id);
			if ((entity != null) && !currentUserIsOwner(entity)) return;
		}

		decoratedRepo.deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		if (mustAddRowLevelSecurity())
		{
			delete(decoratedRepo.findAll(ids));
		}
		else
		{
			decoratedRepo.deleteAll(ids);
		}
	}

	@Override
	public void deleteAll()
	{
		if (mustAddRowLevelSecurity())
		{
			decoratedRepo.forEachBatched(entities -> delete(entities.stream()), 1000);
		}
		else
		{
			decoratedRepo.deleteAll();
		}
	}

	@Override
	public void add(Entity entity)
	{
		if (isOwnedEntityType() && (mustAddRowLevelSecurity() || entity.get(OwnedEntityType.OWNER_USERNAME) == null))
		{
			entity.set(OwnedEntityType.OWNER_USERNAME, SecurityUtils.getCurrentUsername());
		}

		decoratedRepo.add(entity);
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		if (isOwnedEntityType())
		{
			boolean mustAddRowLevelSecurity = mustAddRowLevelSecurity();
			String currentUsername = SecurityUtils.getCurrentUsername();
			entities = entities.map(entity ->
			{
				if (mustAddRowLevelSecurity || entity.get(OwnedEntityType.OWNER_USERNAME) == null)
				{
					entity.set(OwnedEntityType.OWNER_USERNAME, currentUsername);
				}
				return entity;
			});
		}

		return decoratedRepo.add(entities);
	}

	private boolean mustAddRowLevelSecurity()
	{
		return !(SecurityUtils.currentUserIsSu() || SecurityUtils.currentUserHasRole(SystemSecurityToken.ROLE_SYSTEM))
				&& isOwnedEntityType();
	}

	private boolean isOwnedEntityType()
	{
		return EntityUtils.doesExtend(getEntityType(), OWNED);
	}

	private static void addRowLevelSecurity(Query<Entity> q)
	{
		String user = SecurityUtils.getCurrentUsername();
		if (user != null)
		{
			if (!q.getRules().isEmpty()) q.and();
			q.eq(OwnedEntityType.OWNER_USERNAME, user);
		}
	}

	private static String getOwnerUserName(Entity questionnaire)
	{
		return questionnaire.getString(OwnedEntityType.OWNER_USERNAME);
	}

	private static boolean currentUserIsOwner(Entity entity)
	{
		return null != entity && Objects.equals(SecurityUtils.getCurrentUsername(), getOwnerUserName(entity));
	}
}
