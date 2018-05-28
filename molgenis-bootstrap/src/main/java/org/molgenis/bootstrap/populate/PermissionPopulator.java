package org.molgenis.bootstrap.populate;

import org.molgenis.security.core.PermissionService;
import org.molgenis.security.core.PermissionSet;
import org.springframework.context.ApplicationContext;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
		// discover system entity registries
		applicationContext.getBeansOfType(PermissionRegistry.class).values().forEach(this::populate);
	}

	private void populate(PermissionRegistry systemPermissionRegistry)
	{
		systemPermissionRegistry.getPermissions().asMap().forEach((objectIdentity, pairs) ->
		{
			pairs.forEach(pair ->
			{
				PermissionSet permissionSet = pair.getA();
				Sid sid = pair.getB();

				permissionService.grant(objectIdentity, permissionSet, sid);
			});
		});
	}
}
