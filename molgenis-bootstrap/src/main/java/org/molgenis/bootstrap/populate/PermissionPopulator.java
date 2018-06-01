package org.molgenis.bootstrap.populate;

import org.molgenis.security.core.PermissionService;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.util.Pair;
import org.springframework.context.ApplicationContext;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

import static java.util.Objects.requireNonNull;

/**
 * Discovers {@link PermissionRegistry application system permission registries} and populates permissions.
 */
@Component
public class PermissionPopulator
{
	private final PermissionService permissionService;

	public PermissionPopulator(PermissionService permissionService)
	{
		this.permissionService = requireNonNull(permissionService);
	}

	@Transactional
	public void populate(ApplicationContext applicationContext)
	{
		Collection<PermissionRegistry> registries = applicationContext.getBeansOfType(PermissionRegistry.class)
																	  .values();
		registries.forEach(this::populate);
	}

	private void populate(PermissionRegistry systemPermissionRegistry)
	{
		systemPermissionRegistry.getPermissions().asMap().forEach(this::populate);
	}

	private void populate(ObjectIdentity objectIdentity, Collection<Pair<PermissionSet, Sid>> pairs)
	{
		pairs.forEach(pair -> permissionService.grant(objectIdentity, pair.getA(), pair.getB()));
	}
}
