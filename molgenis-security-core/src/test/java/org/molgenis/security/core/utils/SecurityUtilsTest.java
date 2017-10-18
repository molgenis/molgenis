package org.molgenis.security.core.utils;

import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

@TestExecutionListeners(WithSecurityContextTestExecutionListener.class)
@ContextConfiguration(classes = SecurityUtilsTest.Config.class)
public class SecurityUtilsTest extends AbstractMockitoTestNGSpringContextTests
{
	@Test
	@WithMockUser
	public void currentUserIsAuthenticated_true()
	{
		assertTrue(SecurityUtils.currentUserIsAuthenticated());
	}

	@Test
	public void currentUserIsAuthenticatedNoUser_false()
	{
		assertFalse(SecurityUtils.currentUserIsAuthenticated());
	}

	@Test
	@WithAnonymousUser
	public void currentUserIsAuthenticatedAnonymousUser_false()
	{
		assertFalse(SecurityUtils.currentUserIsAuthenticated());
	}

	@Test
	@WithMockUser
	public void currentUserIsSu_false()
	{
		assertFalse(SecurityUtils.currentUserIsSu());
		assertFalse(SecurityUtils.currentUserIsSuOrSystem());
	}

	@WithMockUser(roles = { "SU" })

	@Test
	public void currentUserIsSu_true()
	{
		assertTrue(SecurityUtils.currentUserIsSu());
		assertTrue(SecurityUtils.currentUserIsSuOrSystem());
	}

	@WithMockUser(roles = { "SYSTEM" })
	@Test
	public void currentUserIsSystemTrue() throws Exception
	{
		assertTrue(SecurityUtils.currentUserIsSystem());
		assertTrue(SecurityUtils.currentUserIsSuOrSystem());
	}

	@Test
	@WithMockUser
	public void getCurrentUsername()
	{
		assertEquals(SecurityUtils.getCurrentUsername().get(), "user");
	}

	@Test
	@WithMockUser(roles = { "authority1", "authority2" })
	public void isUserInRole()
	{
		assertTrue(SecurityUtils.currentUserHasRole("ROLE_authority1"));
		assertTrue(SecurityUtils.currentUserHasRole("ROLE_authority2"));
		assertTrue(SecurityUtils.currentUserHasRole("ROLE_authority1", "ROLE_authority2"));
		assertTrue(SecurityUtils.currentUserHasRole("ROLE_authority2", "ROLE_authority1"));
		assertTrue(SecurityUtils.currentUserHasRole("ROLE_authority1", "ROLE_authority3"));
	}

	@Configuration
	public static class Config
	{

	}
}
