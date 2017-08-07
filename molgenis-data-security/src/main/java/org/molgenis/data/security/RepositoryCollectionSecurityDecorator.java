package org.molgenis.data.security;

import org.molgenis.data.*;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.acl.EntityAcl;
import org.molgenis.data.security.acl.EntityAclManager;
import org.molgenis.data.security.acl.EntityIdentity;
import org.molgenis.util.EntityUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * {@link RepositoryCollection} decorator that adds/deletes {@link Entity} access control lists on {@link Repository} updates.
 */
class RepositoryCollectionSecurityDecorator extends AbstractRepositoryCollectionDecorator
{
	private static final int BATCH_SIZE = 1000;

	private final EntityAclManager entityAclManager;

	RepositoryCollectionSecurityDecorator(RepositoryCollection delegateRepositoryCollection,
			EntityAclManager entityAclManager)
	{
		super(delegateRepositoryCollection);
		this.entityAclManager = requireNonNull(entityAclManager);
	}

	@Override
	public Repository<Entity> createRepository(EntityType entityType)
	{
		if (entityType.isEntityLevelSecurity())
		{
			enableEntityLevelSecurity(entityType, false);
		}
		return delegate().createRepository(entityType);
	}

	@Override
	public void deleteRepository(EntityType entityType)
	{
		if (entityType.isEntityLevelSecurity())
		{
			disableEntityLevelSecurity(entityType);
		}
		delegate().deleteRepository(entityType);
	}

	@Override
	public void updateRepository(EntityType entityType, EntityType updatedEntityType)
	{
		if (!entityType.isEntityLevelSecurity() && updatedEntityType.isEntityLevelSecurity())
		{
			enableEntityLevelSecurity(entityType, true);
		}
		else if (entityType.isEntityLevelSecurity() && !updatedEntityType.isEntityLevelSecurity())
		{
			disableEntityLevelSecurity(entityType);
		}
		else
		{
			updateEntityLevelSecurityInheritance(entityType, updatedEntityType);
		}

		delegate().updateRepository(entityType, updatedEntityType);
	}

	private void enableEntityLevelSecurity(EntityType entityType, boolean createAcls)
	{
		entityAclManager.createAclClass(entityType);
		if (createAcls)
		{
			Fetch entityIdFetch = createEntityIdFetch(entityType);
			getRepository(entityType).forEachBatched(entityIdFetch, entityAclManager::createAcls, BATCH_SIZE);
		}
	}

	private void disableEntityLevelSecurity(EntityType entityType)
	{
		entityAclManager.deleteAclClass(entityType);
	}

	private void updateEntityLevelSecurityInheritance(EntityType entityType, EntityType updatedEntityType)
	{
		Attribute entityLevelSecurityInheritance = entityType.getEntityLevelSecurityInheritance();
		Attribute updatedEntityLevelSecurityInheritance = updatedEntityType.getEntityLevelSecurityInheritance();
		if (EntityUtils.equals(entityLevelSecurityInheritance, updatedEntityLevelSecurityInheritance))
		{
			return;
		}

		// update parent for access control lists
		Fetch entityIdFetch = createEntityIdFetch(entityType);
		getRepository(entityType).forEachBatched(entityIdFetch,
				entities -> updateEntityAcls(entities, updatedEntityLevelSecurityInheritance), BATCH_SIZE);
	}

	private void updateEntityAcls(Collection<Entity> entities, Attribute inheritanceAttribute)
	{
		Map<EntityIdentity, Entity> entityIdentityEntityMap = entities.stream()
																	  .collect(toMap(entity -> EntityIdentity.create(
																			  entity.getEntityType().getId(),
																			  entity.getIdValue()),
																			  Function.identity()));

		Collection<EntityAcl> entityAcls = entityAclManager.readAcls(entityIdentityEntityMap.keySet());
		List<EntityAcl> updatedEntityAcls = entityAcls.stream()
													  .map(entityAcl -> createEntityAclParent(entityAcl,
															  inheritanceAttribute,
															  entityIdentityEntityMap))
													  .collect(toList());
		entityAclManager.updateAcls(updatedEntityAcls);
	}

	private EntityAcl createEntityAclParent(EntityAcl entityAcl, Attribute inheritanceAttribute,
			Map<EntityIdentity, Entity> entityIdentityEntityMap)
	{
		EntityAcl parentAcl;
		if (inheritanceAttribute == null)
		{
			parentAcl = null;
		}
		else
		{
			Entity entity = entityIdentityEntityMap.get(entityAcl.getEntityIdentity());
			Entity parentEntity = entity.getEntity(inheritanceAttribute.getName());
			if (parentEntity != null)
			{
				parentAcl = entityAclManager.readAcl(
						EntityIdentity.create(parentEntity.getEntityType().getId(), parentEntity.getIdValue()));
			}
			else
			{
				parentAcl = null;
			}
		}
		return entityAcl.toBuilder().setParent(parentAcl).build();
	}

	private Fetch createEntityIdFetch(EntityType entityType)
	{
		return new Fetch().field(entityType.getIdAttribute().getName());
	}
}
