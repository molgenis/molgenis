package org.molgenis.security.owned;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.support.OwnedEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.util.EntityUtils;

/**
 * RepositoryDecorator that works on EntityMetaData that extends OwnedEntityMetaData.
 * 
 * Ensures that when an Entity is created the owner is set to the current user, users can only view, update, delete
 * their own entities.
 * 
 * Admins are not effected.
 */
public class OwnedEntityRepositoryDecorator implements Repository
{
	private final Repository decoratedRepo;

	public OwnedEntityRepositoryDecorator(Repository decoratedRepo)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		if (mustAddRowLevelSecurity()) return findAll(new QueryImpl()).iterator();
		return decoratedRepo.iterator();
	}

	@Override
	public Stream<Entity> stream(Fetch fetch)
	{
		if (fetch != null)
		{
			fetch.field(OwnedEntityMetaData.ATTR_OWNER_USERNAME);
		}
		Stream<Entity> entities = decoratedRepo.stream(fetch);
		if (mustAddRowLevelSecurity())
		{
			entities = entities.filter(this::currentUserIsOwner);
		}
		return entities;
	}

	@Override
	public void close() throws IOException
	{
		decoratedRepo.close();
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return decoratedRepo.getCapabilities();
	}

	@Override
	public String getName()
	{
		return decoratedRepo.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return decoratedRepo.getEntityMetaData();
	}

	@Override
	public long count()
	{
		if (mustAddRowLevelSecurity()) return count(new QueryImpl());
		return decoratedRepo.count();
	}

	@Override
	public Query query()
	{
		return decoratedRepo.query();
	}

	@Override
	public long count(Query q)
	{
		if (mustAddRowLevelSecurity()) addRowLevelSecurity(q);
		return decoratedRepo.count(q);
	}

	@Override
	public Stream<Entity> findAll(Query q)
	{
		if (mustAddRowLevelSecurity())
		{
			addRowLevelSecurity(q);
		}
		return decoratedRepo.findAll(q);
	}

	@Override
	public Entity findOne(Query q)
	{
		if (mustAddRowLevelSecurity()) addRowLevelSecurity(q);
		return decoratedRepo.findOne(q);
	}

	@Override
	public Entity findOne(Object id)
	{
		Entity e = decoratedRepo.findOne(id);

		if (mustAddRowLevelSecurity())
		{
			if (!currentUserIsOwner(e)) return null;
		}

		return e;
	}

	@Override
	public Entity findOne(Object id, Fetch fetch)
	{
		if (fetch != null)
		{
			fetch.field(OwnedEntityMetaData.ATTR_OWNER_USERNAME);
		}
		Entity e = decoratedRepo.findOne(id, fetch);

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
			entities = entities.filter(this::currentUserIsOwner);
		}
		return entities;
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		if (fetch != null)
		{
			fetch.field(OwnedEntityMetaData.ATTR_OWNER_USERNAME);
		}
		Stream<Entity> entities = decoratedRepo.findAll(ids, fetch);
		if (mustAddRowLevelSecurity())
		{
			entities = entities.filter(this::currentUserIsOwner);
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
		if (mustAddRowLevelSecurity())
			entity.set(OwnedEntityMetaData.ATTR_OWNER_USERNAME, SecurityUtils.getCurrentUsername());
		decoratedRepo.update(entity);
	}

	@Override
	public void update(Stream<? extends Entity> entities)
	{
		if (mustAddRowLevelSecurity())
		{
			entities = entities.filter(entity -> {
				// do not allow owner value changes
				entity.set(OwnedEntityMetaData.ATTR_OWNER_USERNAME, SecurityUtils.getCurrentUsername());
				return true;
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
	public void delete(Stream<? extends Entity> entities)
	{
		if (mustAddRowLevelSecurity())
		{
			entities = entities.filter(this::currentUserIsOwner);
		}

		decoratedRepo.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		if (mustAddRowLevelSecurity())
		{
			Entity entity = findOne(id);
			if ((entity != null) && !currentUserIsOwner(entity)) return;
		}

		decoratedRepo.deleteById(id);
	}

	@Override
	public void deleteById(Stream<Object> ids)
	{
		if (mustAddRowLevelSecurity())
		{
			delete(decoratedRepo.findAll(ids));
		}
		else
		{
			decoratedRepo.deleteById(ids);
		}
	}

	@Override
	public void deleteAll()
	{
		if (mustAddRowLevelSecurity())
		{
			delete(decoratedRepo.stream());
		}
		else
		{
			decoratedRepo.deleteAll();
		}
	}

	@Override
	public void add(Entity entity)
	{
		if (mustAddRowLevelSecurity())
		{
			entity.set(OwnedEntityMetaData.ATTR_OWNER_USERNAME, SecurityUtils.getCurrentUsername());
		}

		decoratedRepo.add(entity);
	}

	@Override
	public Integer add(Stream<? extends Entity> entities)
	{
		if (mustAddRowLevelSecurity())
		{
			entities = entities.map(entity -> {
				entity.set(OwnedEntityMetaData.ATTR_OWNER_USERNAME, SecurityUtils.getCurrentUsername());
				return entity;
			});
		}

		return decoratedRepo.add(entities);
	}

	@Override
	public void flush()
	{
		decoratedRepo.flush();
	}

	@Override
	public void clearCache()
	{
		decoratedRepo.clearCache();
	}

	private boolean mustAddRowLevelSecurity()
	{
		if (SecurityUtils.currentUserIsSu()) return false;
		return EntityUtils.doesExtend(getEntityMetaData(), OwnedEntityMetaData.ENTITY_NAME);
	}

	private void addRowLevelSecurity(Query q)
	{
		String user = SecurityUtils.getCurrentUsername();
		if (user != null)
		{
			if (!q.getRules().isEmpty()) q.and();
			q.eq(OwnedEntityMetaData.ATTR_OWNER_USERNAME, user);
		}
	}

	private String getOwnerUserName(Entity questionnaire)
	{
		return questionnaire.getString(OwnedEntityMetaData.ATTR_OWNER_USERNAME);
	}

	@Override
	public void create()
	{
		decoratedRepo.create();
	}

	@Override
	public void drop()
	{
		decoratedRepo.drop();
	}

	@Override
	public void rebuildIndex()
	{
		decoratedRepo.rebuildIndex();
	}

	@Override
	public void addEntityListener(EntityListener entityListener)
	{
		decoratedRepo.addEntityListener(entityListener);
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		decoratedRepo.removeEntityListener(entityListener);
	}

	private boolean currentUserIsOwner(Entity entity)
	{
		return SecurityUtils.getCurrentUsername().equals(getOwnerUserName(entity));
	}
}
