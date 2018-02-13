package org.molgenis.security.core.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.molgenis.security.core.runas.SystemSecurityToken.ROLE_SYSTEM;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_SU;
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
		when((Collection<GrantedAuthority>) userDetails.getAuthorities()).thenReturn(
				Arrays.asList(authority1, authority2));
		when(authentication.getPrincipal()).thenReturn(userDetails);
		when((Collection<GrantedAuthority>) authentication.getAuthorities()).thenReturn(
				Arrays.asList(authority1, authority2));
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

	@SuppressWarnings("unchecked")
	@Test
	public void currentUserIsAuthenticated_falseAnonymous()
	{
		when(authentication.isAuthenticated()).thenReturn(true);
		GrantedAuthority authoritySu = when(mock(GrantedAuthority.class).getAuthority()).thenReturn("ROLE_ANONYMOUS")
																						.getMock();
		when((Collection<GrantedAuthority>) authentication.getAuthorities()).thenReturn(
				Collections.singletonList(authoritySu));
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
		when((Collection<GrantedAuthority>) authentication.getAuthorities()).thenReturn(
				Collections.singletonList(authoritySu));
		assertTrue(SecurityUtils.currentUserIsSu());
		assertTrue(SecurityUtils.currentUserIsSuOrSystem());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void currentUserIsSystemTrue() throws Exception
	{
		GrantedAuthority authoritySystem = when(mock(GrantedAuthority.class).getAuthority()).thenReturn(ROLE_SYSTEM)
																							.getMock();
		when((Collection<GrantedAuthority>) authentication.getAuthorities()).thenReturn(
				Collections.singletonList(authoritySystem));
		assertTrue(SecurityUtils.currentUserIsSystem());
		assertTrue(SecurityUtils.currentUserIsSuOrSystem());
	}

	@Test
	public void currentUserIsSystemFalse() throws Exception
	{
		when(userDetails.getUsername()).thenReturn("user");
		assertFalse(SecurityUtils.currentUserIsSystem());
		assertFalse(SecurityUtils.currentUserIsSuOrSystem());
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
