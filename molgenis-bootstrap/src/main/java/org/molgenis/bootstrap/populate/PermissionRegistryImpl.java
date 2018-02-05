package org.molgenis.bootstrap.populate;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.molgenis.core.ui.admin.user.UserAccountController;
import org.molgenis.data.DataService;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginPermission;
import org.molgenis.data.security.auth.Group;
import org.molgenis.util.Pair;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.plugin.model.PluginPermissionUtils.getCumulativePermission;
import static org.molgenis.data.security.auth.GroupMetaData.GROUP;
import static org.molgenis.data.security.auth.GroupMetaData.NAME;
import static org.molgenis.security.account.AccountService.ALL_USER_GROUP;
import static org.molgenis.security.acl.SidUtils.createSid;

@Component
public class PermissionRegistryImpl implements PermissionRegistry
{
	private final DataService dataService;

	public PermissionRegistryImpl(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public Multimap<ObjectIdentity, Pair<Permission, Sid>> getPermissions()
	{
		Group allUsersGroup = dataService.query(GROUP, Group.class).eq(NAME, ALL_USER_GROUP).findOne();

		ObjectIdentity pluginIdentity = new PluginIdentity(UserAccountController.ID);
		Permission pluginPermissions = getCumulativePermission(PluginPermission.WRITE);
		return new ImmutableMultimap.Builder<ObjectIdentity, Pair<Permission, Sid>>().putAll(pluginIdentity,
				new Pair<>(pluginPermissions, createSid(allUsersGroup))).build();
	}
}
