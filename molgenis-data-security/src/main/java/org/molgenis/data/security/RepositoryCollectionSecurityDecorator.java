package org.molgenis.data.security;

import org.molgenis.data.*;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.core.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.*;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.RepositoryPermission.WRITEMETA;
import static org.molgenis.data.security.RepositoryPermissionUtils.getCumulativePermission;

public class RepositoryCollectionSecurityDecorator extends AbstractRepositoryCollectionDecorator
{
	private static final Logger LOG = LoggerFactory.getLogger(RepositoryCollectionSecurityDecorator.class);

	private final MutableAclService mutableAclService;
	private final UserPermissionEvaluator userPermissionEvaluator;

	RepositoryCollectionSecurityDecorator(RepositoryCollection delegateRepositoryCollection,
			MutableAclService mutableAclService, UserPermissionEvaluator userPermissionEvaluator)
	{
		super(delegateRepositoryCollection);
		this.mutableAclService = requireNonNull(mutableAclService);
		this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
	}

	@Override
	public Repository<Entity> createRepository(EntityType entityType)
	{
		createAcl(entityType);
		return super.createRepository(entityType);
	}

	@Override
	public void updateRepository(EntityType entityType, EntityType updatedEntityType)
	{
		validateRepositoryPermission(entityType, WRITEMETA);
		updateAcl(entityType, updatedEntityType);
		super.updateRepository(entityType, updatedEntityType);
	}

	@Override
	public void addAttribute(EntityType entityType, Attribute attribute)
	{
		validateRepositoryPermission(entityType, WRITEMETA);
		super.addAttribute(entityType, attribute);
	}

	@Override
	public void updateAttribute(EntityType entityType, Attribute attr, Attribute updatedAttr)
	{
		validateRepositoryPermission(entityType, WRITEMETA);
		super.updateAttribute(entityType, attr, updatedAttr);
	}

	@Override
	public void deleteAttribute(EntityType entityType, Attribute attr)
	{
		validateRepositoryPermission(entityType, WRITEMETA);
		super.deleteAttribute(entityType, attr);
	}

	@Override
	public void deleteRepository(EntityType entityType)
	{
		validateRepositoryPermission(entityType, WRITEMETA);
		super.deleteRepository(entityType);
		mutableAclService.deleteAcl(new RepositoryIdentity(entityType), true);
	}

	private void createAcl(EntityType entityType)
	{
		MutableAcl acl = mutableAclService.createAcl(new RepositoryIdentity(entityType));
		Package package_ = entityType.getPackage();
		if (package_ != null)
		{
			ObjectIdentity objectIdentity = new PackageIdentity(package_);
			Acl parentAcl = mutableAclService.readAclById(objectIdentity);
			acl.setParent(parentAcl);
		}
		Sid sid = new PrincipalSid(SecurityUtils.getCurrentUsername());
		acl.insertAce(acl.getEntries().size(), getCumulativePermission(WRITEMETA), sid, true);
		mutableAclService.updateAcl(acl);
	}

	private void updateAcl(EntityType entityType, EntityType updatedEntityType)
	{
		Package package_ = entityType.getPackage();
		Package updatedPackage = updatedEntityType.getPackage();
		if (updatedPackage != null && (package_ == null || !updatedPackage.getId().equals(package_.getId())))
		{
			MutableAcl acl = (MutableAcl) mutableAclService.readAclById(new RepositoryIdentity(entityType));
			ObjectIdentity objectIdentity = new PackageIdentity(updatedPackage);
			Acl parentAcl = mutableAclService.readAclById(objectIdentity);
			acl.setParent(parentAcl);
			mutableAclService.updateAcl(acl);
		}
		else if (updatedPackage == null && package_ != null)
		{
			MutableAcl acl = (MutableAcl) mutableAclService.readAclById(new RepositoryIdentity(entityType));
			acl.setParent(null);
			mutableAclService.updateAcl(acl);
		}
	}

	private void validateRepositoryPermission(EntityType entityType, RepositoryPermission permission)
	{
		boolean hasPermission = userPermissionEvaluator.hasPermission(new RepositoryIdentity(entityType), permission);
		if (!hasPermission)
		{
			throw new MolgenisDataAccessException(
					format("No [%s] permission on repository [%s] with id [%s]", permission.getPattern(),
							entityType.getLabel(), entityType.getId()));
		}
	}
}