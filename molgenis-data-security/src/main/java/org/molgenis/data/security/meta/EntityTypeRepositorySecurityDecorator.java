package org.molgenis.data.security.meta;

import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.security.*;
import org.molgenis.data.security.exception.EntityTypePermissionDeniedException;
import org.molgenis.data.security.exception.NullPackageNotSuException;
import org.molgenis.data.security.exception.PackagePermissionDeniedException;
import org.molgenis.data.security.exception.SystemMetadataModificationException;
import org.molgenis.data.security.owned.AbstractRowLevelSecurityRepositoryDecorator;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSuOrSystem;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSystem;

/**
 * Decorator for the entity type repository:
 * - filters requested entities based on the permissions of the current user
 * - validates permissions when adding, updating or deleting entity types
 */
public class EntityTypeRepositorySecurityDecorator extends AbstractRowLevelSecurityRepositoryDecorator<EntityType>
{
	private final SystemEntityTypeRegistry systemEntityTypeRegistry;
	private final UserPermissionEvaluator userPermissionEvaluator;
	private final MutableAclService mutableAclService;
	private final MutableAclClassService mutableAclClassService;
	private final DataService dataService;

	public EntityTypeRepositorySecurityDecorator(Repository<EntityType> delegateRepository,
			SystemEntityTypeRegistry systemEntityTypeRegistry, UserPermissionEvaluator userPermissionEvaluator,
			MutableAclService mutableAclService, MutableAclClassService mutableAclClassService, DataService dataService)
	{
		super(delegateRepository, mutableAclService);
		this.systemEntityTypeRegistry = requireNonNull(systemEntityTypeRegistry);
		this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
		this.mutableAclService = requireNonNull(mutableAclService);
		this.mutableAclClassService = requireNonNull(mutableAclClassService);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public boolean isActionPermitted(EntityType entityType, Action action)
	{
		boolean permission = true;
		if (action == Action.CREATE || action == Action.UPDATE)
		{
			checkPackagePermission(entityType, action);
		}
		if (action != Action.CREATE)
		{
			permission = checkEntityTypePermission(entityType.getId(), action);
		}
		return permission;
	}

	@Override
	public boolean isActionPermitted(Object id, Action action)
	{
		if (action == Action.CREATE || action == Action.UPDATE)
		{
			throw new IllegalStateException(
					"CREATE and UPDATE permission checks should use 'isActionPermitted(EntityType entityType, Action action)'");
		}
		return checkEntityTypePermission(id.toString(), action);
	}

	private boolean checkEntityTypePermission(String entityTypeId, Action action)
	{
		EntityTypePermission permission = getPermissionForAction(action);
		boolean hasPermission = userPermissionEvaluator.hasPermission(new EntityTypeIdentity(entityTypeId), permission);
		if (hasPermission && action != Action.COUNT && action != Action.READ)
		{
			boolean isSystem = systemEntityTypeRegistry.hasSystemEntityType(entityTypeId);
			if (isSystem && !currentUserIsSystem())
			{
				throw new SystemMetadataModificationException();
			}
		}
		return hasPermission;
	}

	/**
	 * @return the EntityTypeAction to check given the Action on the repository
	 */
	private static EntityTypePermission getPermissionForAction(Action action)
	{
		EntityTypePermission permission;
		switch (action)
		{
			case COUNT:
			case READ:
				permission = EntityTypePermission.READ_METADATA;
				break;
			case UPDATE:
				permission = EntityTypePermission.UPDATE_METADATA;
				break;
			case DELETE:
				permission = EntityTypePermission.DELETE_METADATA;
				break;
			case CREATE:
				throw new IllegalStateException("Shouldn't check entity types that you're creating");
			default:
				throw new IllegalArgumentException("Illegal repository ACtion");
		}
		return permission;
	}

	@Override
	public void createAcl(EntityType entityType)
	{
		MutableAcl acl = mutableAclService.createAcl(new EntityTypeIdentity(entityType.getId()));
		Package pack = entityType.getPackage();
		if (pack != null)
		{
			ObjectIdentity objectIdentity = new PackageIdentity(pack);
			acl.setParent(mutableAclService.readAclById(objectIdentity));
			mutableAclService.updateAcl(acl);
		}
	}

	@Override
	public void deleteAcl(EntityType entityType)
	{
		mutableAclService.deleteAcl(new EntityTypeIdentity(entityType), true);
		mutableAclClassService.deleteAclClass(EntityIdentityUtils.toType(entityType));
	}

	@Override
	public void deleteAcl(Object entityTypeId)
	{
		mutableAclService.deleteAcl(new EntityTypeIdentity((String) entityTypeId), true);
		mutableAclClassService.deleteAclClass(EntityIdentityUtils.toType((String) entityTypeId));
	}

	@Override
	public void updateAcl(EntityType entityType)
	{
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(new EntityTypeIdentity(entityType.getId()));
		Package pack = entityType.getPackage();
		if (pack != null)
		{
			ObjectIdentity objectIdentity = new PackageIdentity(pack);
			Acl parentAcl = mutableAclService.readAclById(objectIdentity);
			if (!parentAcl.equals(acl.getParentAcl()))
			{
				acl.setParent(parentAcl);
				mutableAclService.updateAcl(acl);
			}
		}
	}

	private void checkPackagePermission(EntityType newEntityType, Action action)
	{
		Package pack = newEntityType.getPackage();
		if (pack != null)
		{
			boolean checkPackage = isPackageUpdated(action, newEntityType);
			if (checkPackage && !userPermissionEvaluator.hasPermission(new PackageIdentity(pack.getId()),
					PackagePermission.ADD_ENTITY_TYPE))
			{
				throw new PackagePermissionDeniedException(PackagePermission.ADD_ENTITY_TYPE, pack);
			}
		}
		else
		{
			if (!currentUserIsSuOrSystem() && isPackageUpdated(action, newEntityType))
			{
				throw new NullPackageNotSuException();
			}
		}
	}

	@Override
	public void throwPermissionException(EntityType entityType, Action action)
	{
		throw new EntityTypePermissionDeniedException(getPermissionForAction(action), entityType);
	}

	private boolean isPackageUpdated(Action action, EntityType newEntityType)
	{
		boolean updated;
		if (action == Action.CREATE)
		{
			updated = true;
		}
		else
		{
			EntityType currentEntityType = dataService.findOneById(EntityTypeMetadata.ENTITY_TYPE_META_DATA,
					newEntityType.getId(), EntityType.class);
			if (currentEntityType.getPackage() == null)
			{
				updated = newEntityType.getPackage() != null;
			}
			else
			{
				if (newEntityType.getPackage() == null)
				{
					updated = true;
				}
				else
				{
					updated = !currentEntityType.getPackage().getId().equals(newEntityType.getPackage().getId());
				}
			}
		}
		return updated;
	}
}