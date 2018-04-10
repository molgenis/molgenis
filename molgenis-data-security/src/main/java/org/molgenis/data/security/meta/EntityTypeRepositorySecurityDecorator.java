package org.molgenis.data.security.meta;

import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.security.*;
import org.molgenis.data.security.owned.AbstractRowLevelSecurityRepositoryDecorator;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.security.acls.domain.AbstractPermission;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSuOrSystem;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSystem;

/**
 * Decorator for the entity type repository:
 * - filters requested entities based on the permissions of the current user
 * - validates permissions when adding, updating or deleting entity types
 * <p>
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

	protected String toMessagePermission(Action action)
	{
		AbstractPermission permission = getPermissionForOperation(action);
		String name;
		if (permission instanceof EntityTypePermission)
		{
			name = ((EntityTypePermission) permission).getName();
		}
		else if (permission instanceof PackagePermission)
		{
			name = ((PackagePermission) permission).getName();
		}
		else
		{
			throw new MolgenisDataException("Unexpected permission type");
		}
		return name;
	}

	@Override
	public boolean isOperationPermitted(EntityType entityType, Action action)
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
	public boolean isOperationPermitted(Object id, Action action)
	{
		if (action == Action.CREATE || action == Action.UPDATE)
		{
			throw new UnexpectedEnumException(action);
		}
		return checkEntityTypePermission(id, action);
	}

	private boolean checkEntityTypePermission(Object id, Action action)
	{
		AbstractPermission permission = getPermissionForOperation(action);
		boolean hasPermission = userPermissionEvaluator.hasPermission(new EntityTypeIdentity(id.toString()),
				permission);
		if (hasPermission && !permission.equals(EntityTypePermission.COUNT))
		{
			boolean isSystem = systemEntityTypeRegistry.hasSystemEntityType(id.toString());
			if (isSystem && !currentUserIsSystem())
			{
				throw new MolgenisDataException(
						format("No [%s] permission on EntityType [%s]", toMessagePermission(action), id));
			}
		}
		return hasPermission;
	}

	private static AbstractPermission getPermissionForOperation(Action action)
	{
		AbstractPermission permission;
		switch (action)
		{
			case COUNT:
			case READ:
				permission = EntityTypePermission.COUNT;
				break;
			case UPDATE:
			case DELETE:
				permission = EntityTypePermission.WRITEMETA;
				break;
			case CREATE:
				permission = PackagePermission.WRITEMETA;
				break;
			default:
				throw new IllegalArgumentException("Illegal entity type permission");
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

	private void checkPackagePermission(EntityType newEntityType,
			AbstractRowLevelSecurityRepositoryDecorator.Action action)
	{
		Package pack = newEntityType.getPackage();
		if (pack != null)
		{
			boolean checkPackage = action == AbstractRowLevelSecurityRepositoryDecorator.Action.CREATE || (
					action == AbstractRowLevelSecurityRepositoryDecorator.Action.UPDATE && isPackageUpdated(
							newEntityType));
			if (checkPackage && !userPermissionEvaluator.hasPermission(new PackageIdentity(pack.getId()),
					PackagePermission.WRITEMETA))
			{
				throw new MolgenisDataException(
						String.format("No [%s] permission on package '%s'", toMessagePermission(action),
								pack.getLabel()));
			}
		}
		else
		{
			if (!currentUserIsSuOrSystem())
			{
				throw new MolgenisDataException("Only superusers are allowed to create EntityTypes without a package.");
			}
		}
	}

	private boolean isPackageUpdated(EntityType newEntityType)
	{
		EntityType currentEntityType = dataService.findOneById(EntityTypeMetadata.ENTITY_TYPE_META_DATA,
				newEntityType.getId(), EntityType.class);
		return currentEntityType.getPackage() == null || !currentEntityType.getPackage()
																		   .equals(newEntityType.getPackage());
	}
}