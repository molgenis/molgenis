package org.molgenis.bootstrap.populate;

import org.springframework.context.ApplicationContext;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;

/**
 * Discovers {@link PermissionRegistry application system permission registries} and populates permissions.
 */
@Component
public class PermissionPopulator
{
	private final MutableAclService mutableAclService;

	public PermissionPopulator(MutableAclService mutableAclService)
	{
		this.mutableAclService = requireNonNull(mutableAclService);
	}

	@Transactional
	public void populate(ApplicationContext applicationContext)
	{
		// discover system entity registries
		applicationContext.getBeansOfType(PermissionRegistry.class).values().forEach(this::populate);
	}

	private void populate(PermissionRegistry systemPermissionRegistry)
	{
		systemPermissionRegistry.getPermissions().asMap().forEach((objectIdentity, pairs) ->
		{
			MutableAcl acl = (MutableAcl) mutableAclService.readAclById(objectIdentity);
			pairs.forEach(pair -> acl.insertAce(acl.getEntries().size(), pair.getA(), pair.getB(), true));
			mutableAclService.updateAcl(acl);
		});
	}
}
