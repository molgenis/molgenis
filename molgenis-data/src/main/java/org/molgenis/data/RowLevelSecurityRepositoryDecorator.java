package org.molgenis.data;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.runas.SystemSecurityToken;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.*;
import java.util.stream.*;

import static autovalue.shaded.com.google.common.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.RowLevelSecurityUtils.hasPermission;
import static org.molgenis.data.RowLevelSecurityUtils.validatePermission;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

public class RowLevelSecurityRepositoryDecorator implements Repository
{
	public static final String UPDATE_ATTRIBUTE = "_" + Permission.UPDATE.toString();
	private static final List<String> ROW_LEVEL_SECURITY_ATTRIBUTES = Collections.singletonList(UPDATE_ATTRIBUTE);
	public static final String PERMISSIONS_ATTRIBUTE = "_PERMISSIONS";

	private final Repository decoratedRepository;

	public RowLevelSecurityRepositoryDecorator(Repository decoratedRepository)
	{
		this.decoratedRepository = requireNonNull(decoratedRepository);
	}

	@Override
	public Stream<Entity> stream(Fetch fetch)
	{
		return decoratedRepository.stream(fetch).map(this::injectPermissions);
	}

	@Override
	public void close() throws IOException
	{
		decoratedRepository.close();
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return decoratedRepository.getCapabilities();
	}

	@Override
	public String getName()
	{
		return decoratedRepository.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		if (isRowLevelSecured() && !isCurrentUserSuOrSystem())
		{
			return new RowLevelSecurityEntityMetaData(decoratedRepository.getEntityMetaData());
		}
		else
		{
			return decoratedRepository.getEntityMetaData();
		}
	}

	@Override
	public long count()
	{
		return decoratedRepository.count();
	}

	@Override
	public Iterator<Entity> iterator()
	{
		final Iterator<Entity> iterator = decoratedRepository.iterator();
		return new Iterator<Entity>()
		{
			@Override
			public boolean hasNext()
			{
				return iterator.hasNext();
			}

			@Override
			public Entity next()
			{
				Entity entity = iterator.next();
				injectPermissions(entity);
				return entity;
			}
		};
	}

	@Override
	public Query query()
	{
		return decoratedRepository.query();
	}

	@Override
	public long count(Query q)
	{
		return decoratedRepository.count(q);
	}

	@Override
	public Stream<Entity> findAll(Query q)
	{
		if (isRowLevelSecured())
		{
			return decoratedRepository.findAll(q).map(this::injectPermissions);
		}
		else
		{
			return decoratedRepository.findAll(q);
		}
	}

	@Override
	public Entity findOne(Query q)
	{
		Entity entity = decoratedRepository.findOne(q);
		if (entity != null && isRowLevelSecured())
		{
			return injectPermissions(entity);
		}
		return entity;
	}

	@Override
	public Entity findOne(Object id)
	{
		Entity entity = decoratedRepository.findOne(id);
		if (entity != null && isRowLevelSecured())
		{
			return injectPermissions(entity);
		}
		return entity;
	}

	@Override
	public Entity findOne(Object id, Fetch fetch)
	{
		Entity entity = decoratedRepository.findOne(id, fetch);
		if (entity != null && isRowLevelSecured())
		{
			return injectPermissions(entity);
		}
		return entity;
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		if (isRowLevelSecured())
		{
			return decoratedRepository.findAll(ids).map(this::injectPermissions);
		}
		else
		{
			return decoratedRepository.findAll(ids);
		}
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		if (isRowLevelSecured())
		{
			return decoratedRepository.findAll(ids, fetch).map(this::injectPermissions);
		}
		else
		{
			return decoratedRepository.findAll(ids, fetch);
		}
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return decoratedRepository.aggregate(aggregateQuery);
	}

	@Override
	public void update(Entity entity)
	{
		if (isRowLevelSecured() && !isCurrentUserSuOrSystem())
		{
			Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
			Entity completeEntity = getCompleteEntity(entity);
			validatePermission(completeEntity, Permission.UPDATE, currentAuthentication);
			runAsSystem(() -> decoratedRepository.update(completeEntity));
		}
		else
		{
			decoratedRepository.update(entity);
		}
	}

	@Override
	public void update(Stream<? extends Entity> entities)
	{
		if (isRowLevelSecured() && !isCurrentUserSuOrSystem())
		{
			Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
			Stream<? extends Entity> completeEntities = entities.map(this::getCompleteEntity)
					.filter(entity -> validatePermission(entity, Permission.UPDATE, currentAuthentication));
			runAsSystem(() -> decoratedRepository.update(completeEntities));
		}
		else
		{
			decoratedRepository.update(entities);
		}
	}

	@Override
	public void delete(Entity entity)
	{
		if (isRowLevelSecured() && !isCurrentUserSuOrSystem())
		{
			validatePermission(entity, Permission.UPDATE, SecurityContextHolder.getContext().getAuthentication());
			runAsSystem(() -> decoratedRepository.delete(entity));
		}
		else
		{
			decoratedRepository.delete(entity);
		}
	}

	@Override
	public void delete(Stream<? extends Entity> entities)
	{
		if (isRowLevelSecured() && !isCurrentUserSuOrSystem())
		{
			Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
			Stream<? extends Entity> filteredEntities = entities.map(this::getCompleteEntity)
					.filter(entity -> validatePermission(entity, Permission.UPDATE, currentAuthentication));
			runAsSystem(() -> decoratedRepository.delete(filteredEntities));
		}
		else
		{
			decoratedRepository.delete(entities);
		}
	}

	@Override
	public void deleteById(Object id)
	{
		if (isRowLevelSecured() && !isCurrentUserSuOrSystem())
		{
			Entity entity = runAsSystem(() -> findOne(id));
			if (entity != null)
			{
				validatePermission(entity, Permission.UPDATE, SecurityContextHolder.getContext().getAuthentication());
				runAsSystem(() -> decoratedRepository.deleteById(id));
			}
			else
			{
				throw new UnknownEntityException(
						"The entity you are trying to delete with id [" + id.toString() + "] doesn't exist");
			}
		}
		else
		{
			decoratedRepository.deleteById(id);
		}
	}

	@Override
	public void deleteById(Stream<Object> ids)
	{
		if (isRowLevelSecured() && !isCurrentUserSuOrSystem())
		{
			Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
			Stream<Object> filteredIds = ids.filter(id -> {
				Entity entity = runAsSystem(() -> findOne(id));
				if (entity == null) throw new UnknownEntityException(
						"The entity you are trying to delete with id [" + id.toString() + "] doesn't exist");

				return validatePermission(runAsSystem(() -> findOne(id)), Permission.UPDATE, currentAuthentication);
			});
			runAsSystem(() -> decoratedRepository.deleteById(filteredIds));
		}
		else
		{
			decoratedRepository.deleteById(ids);
		}
	}

	@Override
	public void deleteAll()
	{
		if (isRowLevelSecured() && !isCurrentUserSuOrSystem())
		{
			Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
			stream().map(this::getCompleteEntity)
					.forEach(entity -> validatePermission(entity, Permission.UPDATE, currentAuthentication));
			runAsSystem(decoratedRepository::deleteAll);
		}
		else
		{
			decoratedRepository.deleteAll();
		}
	}

	@Override
	public void add(Entity entity)
	{
		decoratedRepository.add(entity);
	}

	@Override
	public Integer add(Stream<? extends Entity> entities)
	{
		return decoratedRepository.add(entities);
	}

	@Override
	public void flush()
	{
		decoratedRepository.flush();
	}

	@Override
	public void clearCache()
	{
		decoratedRepository.clearCache();
	}

	@Override
	public void create()
	{
		decoratedRepository.create();
	}

	@Override
	public void drop()
	{
		decoratedRepository.drop();
	}

	@Override
	public void rebuildIndex()
	{
		decoratedRepository.rebuildIndex();
	}

	@Override
	public void addEntityListener(EntityListener entityListener)
	{
		decoratedRepository.addEntityListener(entityListener);
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		decoratedRepository.removeEntityListener(entityListener);
	}

	private boolean isRowLevelSecured()
	{
		return decoratedRepository.getEntityMetaData().isRowLevelSecured();
	}

	private boolean isCurrentUserSuOrSystem()
	{
		return SecurityUtils.currentUserIsSu() || SecurityUtils.currentUserHasRole(SystemSecurityToken.ROLE_SYSTEM);
	}

	private Entity injectPermissions(Entity entity)
	{
		List<String> permissions = newArrayList();
		if (hasPermission(entity, Permission.UPDATE, SecurityContextHolder.getContext().getAuthentication()))
		{
			permissions.add(UPDATE_ATTRIBUTE);
		}

		entity.set(PERMISSIONS_ATTRIBUTE, StringUtils.join(permissions, ','));
		return entity;
	}

	private Entity getCompleteEntity(Entity entity)
	{
		if (entity.getEntityMetaData().getAttribute(UPDATE_ATTRIBUTE) == null)
		{
			Entity currentEntity = runAsSystem(() -> findOne(entity.getIdValue()));
			Iterable<Entity> users = runAsSystem(() -> currentEntity.getEntities(UPDATE_ATTRIBUTE));
			entity.set(UPDATE_ATTRIBUTE, users);
		}
		return entity;
	}

	private class RowLevelSecurityEntityMetaData extends DefaultEntityMetaData implements EntityMetaData
	{
		public RowLevelSecurityEntityMetaData(EntityMetaData entityMetaData)
		{
			super(entityMetaData);
		}

		@Override
		public Iterable<AttributeMetaData> getAttributes()
		{
			return filterPermissionAttributes(super.getAttributes());
		}

		@Override
		public Iterable<AttributeMetaData> getOwnAttributes()
		{
			return filterPermissionAttributes(super.getOwnAttributes());
		}

		@Override
		public Iterable<AttributeMetaData> getAtomicAttributes()
		{
			return filterPermissionAttributes(super.getAtomicAttributes());
		}

		@Override
		public Iterable<AttributeMetaData> getOwnAtomicAttributes()
		{
			return filterPermissionAttributes(super.getOwnAtomicAttributes());
		}

		@Override
		public AttributeMetaData getAttribute(String attributeName)
		{
			return filterPermissionAttribute(super.getAttribute(attributeName));
		}

		private List<AttributeMetaData> filterPermissionAttributes(Iterable<AttributeMetaData> attributes)
		{
			return StreamSupport.stream(attributes.spliterator(), false)
					.filter(attr -> !ROW_LEVEL_SECURITY_ATTRIBUTES.contains(attr.getName())
							|| SecurityUtils.currentUserIsSu()
							|| SecurityUtils.currentUserHasRole(SystemSecurityToken.ROLE_SYSTEM))
					.collect(Collectors.toList());
		}

		private AttributeMetaData filterPermissionAttribute(AttributeMetaData amd)
		{
			if (!ROW_LEVEL_SECURITY_ATTRIBUTES.contains(amd.getName()) || isCurrentUserSuOrSystem()) return amd;
			return null;
		}
	}
}
