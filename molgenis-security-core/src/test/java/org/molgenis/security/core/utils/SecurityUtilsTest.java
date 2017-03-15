package org.molgenis.security.core.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.molgenis.security.core.runas.SystemSecurityToken.USER_SYSTEM;
import static org.molgenis.security.core.utils.SecurityUtils.*;
import static org.testng.Assert.*;

public class SecurityUtilsTest
{
	private static Authentication AUTHENTICATION_PREVIOUS;
	private Authentication authentication;
	private UserDetails userDetails;

	@BeforeClass
	public void setUpBeforeClass()
	{
		AUTHENTICATION_PREVIOUS = SecurityContextHolder.getContext().getAuthentication();
		authentication = mock(Authentication.class);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		reset(authentication);

		GrantedAuthority authority1 = when(mock(GrantedAuthority.class).getAuthority()).thenReturn("authority1")
				.getMock();
		GrantedAuthority authority2 = when(mock(GrantedAuthority.class).getAuthority()).thenReturn("authority2")
				.getMock();
		userDetails = mock(UserDetails.class);
		when(userDetails.getUsername()).thenReturn("username");
		when(userDetails.getPassword()).thenReturn("encoded-password");
		when((Collection<GrantedAuthority>) userDetails.getAuthorities())
				.thenReturn(Arrays.<GrantedAuthority>asList(authority1, authority2));
		when(authentication.getPrincipal()).thenReturn(userDetails);
		when((Collection<GrantedAuthority>) authentication.getAuthorities())
				.thenReturn(Arrays.<GrantedAuthority>asList(authority1, authority2));
	}

	@AfterClass
	public static void tearDownAfterClass()
	{
		SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_PREVIOUS);
	}

	@Test
	public void currentUserIsAuthenticated_true()
	{
		when(authentication.isAuthenticated()).thenReturn(true);
		assertTrue(SecurityUtils.currentUserIsAuthenticated());
	}

	@Test
	public void currentUserIsAuthenticated_false()
	{
		when(authentication.isAuthenticated()).thenReturn(false);
		assertFalse(SecurityUtils.currentUserIsAuthenticated());
	}

	@Test
	public void currentUserIsAuthenticated_falseAnonymous()
	{
		when(userDetails.getUsername()).thenReturn(ANONYMOUS_USERNAME);
		when(authentication.isAuthenticated()).thenReturn(true);
		assertFalse(SecurityUtils.currentUserIsAuthenticated());
	}

	@Test
	public void currentUserIsSu_false()
	{
		assertFalse(SecurityUtils.currentUserIsSu());
		assertFalse(SecurityUtils.currentUserIsSuOrSystem());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void currentUserIsSu_true()
	{
		GrantedAuthority authoritySu = when(mock(GrantedAuthority.class).getAuthority()).thenReturn(AUTHORITY_SU)
				.getMock();
		when((Collection<GrantedAuthority>) authentication.getAuthorities())
				.thenReturn(Arrays.<GrantedAuthority>asList(authoritySu));
		assertTrue(SecurityUtils.currentUserIsSu());
		assertTrue(SecurityUtils.currentUserIsSuOrSystem());
	}

	@Test
	public void currentUserIsSystemTrue() throws Exception
	{
		when(userDetails.getUsername()).thenReturn(USER_SYSTEM);
		assertTrue(SecurityUtils.currentUserisSystem());
		assertTrue(SecurityUtils.currentUserIsSuOrSystem());
	}

	@Test
	public void currentUserIsSystemFalse() throws Exception
	{
		when(userDetails.getUsername()).thenReturn("user");
		assertFalse(SecurityUtils.currentUserisSystem());
		assertFalse(SecurityUtils.currentUserIsSuOrSystem());
	}

	@Test
	public void defaultPluginAuthorities()
	{
		String pluginId = "plugin1";
		String[] defaultPluginAuthorities = SecurityUtils.defaultPluginAuthorities(pluginId);
		assertEquals(defaultPluginAuthorities, new String[] { AUTHORITY_SU, AUTHORITY_PLUGIN_READ_PREFIX + pluginId,
				AUTHORITY_PLUGIN_WRITE_PREFIX + pluginId });
	}

	@Test
	public void getCurrentUsername()
	{
		assertEquals(SecurityUtils.getCurrentUsername(), userDetails.getUsername());
	}

	@Test
	public void isUserInRole()
	{
		assertTrue(SecurityUtils.currentUserHasRole("authority1"));
		assertTrue(SecurityUtils.currentUserHasRole("authority2"));
		assertTrue(SecurityUtils.currentUserHasRole("authority1", "authority2"));
		assertTrue(SecurityUtils.currentUserHasRole("authority2", "authority1"));
		assertTrue(SecurityUtils.currentUserHasRole("authority1", "authority3"));
	}

	@Test
	public void getEntityAuthorities()
	{
		List<String> authorities = SecurityUtils.getEntityAuthorities("test");
		List<String> expected = Arrays
				.asList("ROLE_ENTITY_READ_test", "ROLE_ENTITY_WRITE_test", "ROLE_ENTITY_COUNT_test",
						"ROLE_ENTITY_NONE_test", "ROLE_ENTITY_WRITEMETA_test");

		Assert.assertEqualsNoOrder(authorities.toArray(), expected.toArray());
	}
}
