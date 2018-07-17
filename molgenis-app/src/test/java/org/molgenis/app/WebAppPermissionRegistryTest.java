package org.molgenis.app;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import org.molgenis.app.controller.HomeController;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.util.Pair;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.testng.annotations.Test;

import java.util.Collection;

import static com.google.common.collect.ImmutableSet.copyOf;
import static org.molgenis.security.core.PermissionSet.READ;
import static org.testng.Assert.assertEquals;

public class WebAppPermissionRegistryTest
{

	@Test
	public void testGetPermissions()
	{
		Multimap<ObjectIdentity, Pair<PermissionSet, Sid>> permissions = new WebAppPermissionRegistry().getPermissions();
		assertEquals(permissions.size(), 45);
		Collection<Pair<PermissionSet, Sid>> pairs = permissions.get(new PluginIdentity(HomeController.ID));
		assertEquals(copyOf(pairs), ImmutableSet.of(new Pair<>(READ, new GrantedAuthoritySid("ROLE_ANONYMOUS")),
				new Pair<>(READ, new GrantedAuthoritySid("ROLE_USER"))));
	}
}