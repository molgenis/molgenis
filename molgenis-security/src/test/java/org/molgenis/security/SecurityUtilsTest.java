package org.molgenis.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.security.SecurityUtils.ANONYMOUS_USERNAME;
import static org.molgenis.security.SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX;
import static org.molgenis.security.SecurityUtils.AUTHORITY_PLUGIN_WRITE_PREFIX;
import static org.molgenis.security.SecurityUtils.AUTHORITY_SU;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SecurityUtilsTest
{
	private Authentication authentication;
	private UserDetails userDetails;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUp()
	{
		authentication = mock(Authentication.class);

		GrantedAuthority authority1 = when(mock(GrantedAuthority.class).getAuthority()).thenReturn("authority1")
				.getMock();
		GrantedAuthority authority2 = when(mock(GrantedAuthority.class).getAuthority()).thenReturn("authority2")
				.getMock();
		userDetails = mock(UserDetails.class);
		when(userDetails.getUsername()).thenReturn("username");
		when(userDetails.getPassword()).thenReturn("encoded-password");
		when((Collection<GrantedAuthority>) userDetails.getAuthorities()).thenReturn(
				Arrays.<GrantedAuthority> asList(authority1, authority2));
		when(authentication.getPrincipal()).thenReturn(userDetails);
		when((Collection<GrantedAuthority>) authentication.getAuthorities()).thenReturn(
				Arrays.<GrantedAuthority> asList(authority1, authority2));
		SecurityContextHolder.getContext().setAuthentication(authentication);
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
	}

	@SuppressWarnings("unchecked")
	@Test
	public void currentUserIsSu_true()
	{
		GrantedAuthority authoritySu = when(mock(GrantedAuthority.class).getAuthority()).thenReturn(AUTHORITY_SU)
				.getMock();
		when((Collection<GrantedAuthority>) authentication.getAuthorities()).thenReturn(
				Arrays.<GrantedAuthority> asList(authoritySu));
		assertTrue(SecurityUtils.currentUserIsSu());
	}

	@Test
	public void defaultPluginAuthorities()
	{
		String pluginId = "plugin1";
		String[] defaultPluginAuthorities = SecurityUtils.defaultPluginAuthorities(pluginId);
		assertEquals(defaultPluginAuthorities,
				new String[]
				{ AUTHORITY_SU, AUTHORITY_PLUGIN_READ_PREFIX + pluginId.toUpperCase(),
						AUTHORITY_PLUGIN_WRITE_PREFIX + pluginId.toUpperCase() });
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
}
