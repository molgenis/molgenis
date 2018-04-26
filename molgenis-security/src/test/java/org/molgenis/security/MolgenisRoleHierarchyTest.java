package org.molgenis.security;

import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

public class MolgenisRoleHierarchyTest extends AbstractMockitoTest
{
	private MolgenisRoleHierarchy molgenisRoleHierarchy;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		molgenisRoleHierarchy = new MolgenisRoleHierarchy();
	}

	@Test
	public void testGetReachableGrantedAuthoritiesSu()
	{
		assertEquals(molgenisRoleHierarchy.getReachableGrantedAuthorities(
				singletonList(new SimpleGrantedAuthority("ROLE_SU"))),
				asList(new SimpleGrantedAuthority("ROLE_ACL_TAKE_OWNERSHIP"),
						new SimpleGrantedAuthority("ROLE_ACL_MODIFY_AUDITING"),
						new SimpleGrantedAuthority("ROLE_ACL_GENERAL_CHANGES"), new SimpleGrantedAuthority("ROLE_SU")));
	}
}