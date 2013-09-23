package org.molgenis.security.permission;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.molgenis.framework.server.MolgenisPermissionService.Permission;
import org.molgenis.security.SecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MolgenisPermissionServiceImplTest
{
	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUp()
	{
		Authentication authentication = mock(Authentication.class);
		GrantedAuthority authority1 = when(mock(GrantedAuthority.class).getAuthority()).thenReturn(
				SecurityUtils.AUTHORITY_ENTITY_PREFIX + Permission.READ + "_ENTITY1").getMock();
		GrantedAuthority authority2 = when(mock(GrantedAuthority.class).getAuthority()).thenReturn(
				SecurityUtils.AUTHORITY_ENTITY_PREFIX + Permission.WRITE + "_ENTITY2").getMock();
		GrantedAuthority authority3 = when(mock(GrantedAuthority.class).getAuthority()).thenReturn(
				SecurityUtils.AUTHORITY_PLUGIN_PREFIX + Permission.READ + "_PLUGIN1").getMock();
		GrantedAuthority authority4 = when(mock(GrantedAuthority.class).getAuthority()).thenReturn(
				SecurityUtils.AUTHORITY_PLUGIN_PREFIX + Permission.WRITE + "_PLUGIN2").getMock();

		when((Collection<GrantedAuthority>) (authentication.getAuthorities())).thenReturn(
				Arrays.<GrantedAuthority> asList(authority1, authority2, authority3, authority4));
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Test
	public void hasPermissionOnEntity()
	{
		assertTrue(new MolgenisPermissionServiceImpl().hasPermissionOnEntity("entity1", Permission.READ));
		assertFalse(new MolgenisPermissionServiceImpl().hasPermissionOnEntity("entity1", Permission.WRITE));
		assertFalse(new MolgenisPermissionServiceImpl().hasPermissionOnEntity("entity2", Permission.READ));
		assertTrue(new MolgenisPermissionServiceImpl().hasPermissionOnEntity("entity2", Permission.WRITE));
		assertFalse(new MolgenisPermissionServiceImpl().hasPermissionOnEntity("entity-unknown", Permission.READ));
	}

	@Test
	public void hasPermissionOnPlugin()
	{
		assertTrue(new MolgenisPermissionServiceImpl().hasPermissionOnPlugin("plugin1", Permission.READ));
		assertFalse(new MolgenisPermissionServiceImpl().hasPermissionOnPlugin("plugin1", Permission.WRITE));
		assertFalse(new MolgenisPermissionServiceImpl().hasPermissionOnPlugin("plugin2", Permission.READ));
		assertTrue(new MolgenisPermissionServiceImpl().hasPermissionOnPlugin("plugin2", Permission.WRITE));
		assertFalse(new MolgenisPermissionServiceImpl().hasPermissionOnPlugin("plugin-unknown", Permission.READ));
	}
}
