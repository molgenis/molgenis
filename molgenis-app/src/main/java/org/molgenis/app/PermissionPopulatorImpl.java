package org.molgenis.app;

import org.molgenis.app.controller.HomeController;
import org.molgenis.bootstrap.populate.PermissionPopulator;
import org.molgenis.data.security.acl.*;
import org.molgenis.ui.admin.user.UserAccountController;
import org.springframework.stereotype.Component;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.bootstrap.populate.UsersGroupsAuthoritiesPopulatorImpl.ROLE_USER_ID;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;
import static org.molgenis.security.core.Permission.READ;
import static org.molgenis.security.core.Permission.WRITE;
import static org.molgenis.security.core.utils.SecurityUtils.ANONYMOUS_USERNAME;
import static org.molgenis.ui.PluginMetadata.PLUGIN;

@Component
public class PermissionPopulatorImpl implements PermissionPopulator
{
	private final EntityAclManager entityAclManager;

	public PermissionPopulatorImpl(EntityAclManager entityAclManager)
	{
		this.entityAclManager = requireNonNull(entityAclManager);
	}

	@Override
	public void populate()
	{
		SecurityId roleUserSecurityId = SecurityId.create(null, ROLE_USER_ID);
		EntityAce roleUserReadAce = EntityAce.create(READ, roleUserSecurityId, true);

		// allow user role to see system package
		EntityAcl entityTypeAcl = entityAclManager.readAcl(EntityIdentity.create(PACKAGE, PACKAGE_SYSTEM));
		entityTypeAcl = entityTypeAcl.toBuilder().setEntries(singletonList(roleUserReadAce)).build();
		entityAclManager.updateAcl(entityTypeAcl);

		// allow anonymous user and user role to see the home plugin
		EntityAcl homePluginAcl = entityAclManager.readAcl(EntityIdentity.create(PLUGIN, HomeController.ID));
		EntityAce homePluginAnonymousAce = EntityAce.create(READ, SecurityId.create(ANONYMOUS_USERNAME, null), true);
		homePluginAcl = homePluginAcl.toBuilder().setEntries(asList(homePluginAnonymousAce, roleUserReadAce)).build();
		entityAclManager.updateAcl(homePluginAcl);

		// allow user role to update profile
		EntityAcl userAccountPluginAcl = entityAclManager.readAcl(
				EntityIdentity.create(PLUGIN, UserAccountController.ID));
		EntityAce userAccountPluginUserAce = EntityAce.create(WRITE, roleUserSecurityId, true);
		userAccountPluginAcl = userAccountPluginAcl.toBuilder()
												   .setEntries(singletonList(userAccountPluginUserAce))
												   .build();
		entityAclManager.updateAcl(userAccountPluginAcl);
	}
}
