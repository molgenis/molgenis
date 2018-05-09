package org.molgenis.security.acl;

import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.User;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class SidUtilsTest
{
	@Test
	public void testCreateSidUser()
	{
		User user = when(mock(User.class).getUsername()).thenReturn("username").getMock();
		Sid sid = SidUtils.createSid(user);
		assertEquals(sid, new PrincipalSid("username"));
	}

	@Test
	public void testCreateSidUsernameAnonymous()
	{
		Sid sid = SidUtils.createSid("anonymous");
		assertEquals(sid, new GrantedAuthoritySid("ROLE_ANONYMOUS"));
	}

	@Test
	public void testCreateSidGroup()
	{
		Role role = when(mock(Role.class).getId()).thenReturn("groupId").getMock();
		Sid sid = SidUtils.createSid(role);
		assertEquals(sid, new GrantedAuthoritySid(new SimpleGrantedAuthority("ROLE_groupId")));
	}

	@Test
	public void testCreateGroupAuthority()
	{
		Role role = when(mock(Role.class).getId()).thenReturn("groupId").getMock();
		assertEquals("ROLE_groupId", SidUtils.createRoleAuthority(role));
	}
}