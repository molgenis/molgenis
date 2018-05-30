package org.molgenis.app;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.molgenis.app.controller.HomeController;
import org.molgenis.bootstrap.populate.PermissionRegistry;
import org.molgenis.data.DataService;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.security.SidUtils;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.util.Pair;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.SidUtils.createRoleSid;
import static org.molgenis.security.account.AccountService.ROLE_USER;
import static org.molgenis.security.core.utils.SecurityUtils.ANONYMOUS_USERNAME;

/**
 * Registry of permissions specific for this web application.
 */
@Component
public class WebAppPermissionRegistry implements PermissionRegistry
{
	private final DataService dataService;

	public WebAppPermissionRegistry(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public Multimap<ObjectIdentity, Pair<PermissionSet, Sid>> getPermissions()
	{
		ObjectIdentity pluginIdentity = new PluginIdentity(HomeController.ID);
		ImmutableMultimap.Builder<ObjectIdentity, Pair<PermissionSet, Sid>> builder = new ImmutableMultimap.Builder<>();
		builder.putAll(pluginIdentity, new Pair<>(PermissionSet.READ, SidUtils.createUserSid(ANONYMOUS_USERNAME)),
				new Pair<>(PermissionSet.READ, createRoleSid(ROLE_USER)));
		return builder.build();
	}
}
