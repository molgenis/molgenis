package org.molgenis.security.core.runas;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class SystemSecurityTokenTest
{

	@Test
	public void SystemSecurityToken()
	{
		assertTrue(new SystemSecurityToken().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SYSTEM")));
	}

	@Test
	public void getCredentials()
	{
		assertNotNull(new SystemSecurityToken().getCredentials());
	}

	@Test
	public void getPrincipal()
	{
		assertNotNull(new SystemSecurityToken().getPrincipal());
	}
}
