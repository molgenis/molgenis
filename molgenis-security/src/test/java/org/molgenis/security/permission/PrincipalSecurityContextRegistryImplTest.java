package org.molgenis.security.permission;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { PrincipalSecurityContextRegistryImplTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class PrincipalSecurityContextRegistryImplTest extends AbstractMockitoTestNGSpringContextTests
{
	@Autowired
	private Config config;

	@Autowired
	private SecurityContextRegistry securityContextRegistry;

	private PrincipalSecurityContextRegistryImpl principalSecurityContextRegistryImpl;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		config.resetMocks();
		principalSecurityContextRegistryImpl = new PrincipalSecurityContextRegistryImpl(securityContextRegistry);
	}

	@WithMockUser(username = "user")
	@Test
	public void testGetSecurityContextsUserThreadNoUserSessions()
	{
		SecurityContext securityContext = SecurityContextHolder.getContext();
		Object user = securityContext.getAuthentication().getPrincipal();
		assertEquals(principalSecurityContextRegistryImpl.getSecurityContexts(user).collect(toList()),
				singletonList(securityContext));
	}

	@WithMockUser(username = "systemUser")
	@Test
	public void testGetSecurityContextsNoUserThreadNoUserSessions()
	{
		Object user = when(mock(User.class).getUsername()).thenReturn("user").getMock();
		assertEquals(principalSecurityContextRegistryImpl.getSecurityContexts(user).collect(toList()), emptyList());
	}

	@WithMockUser(username = "user")
	@Test
	public void testGetSecurityContextsUserThreadSessions()
	{
		Authentication userAuthentication = SecurityContextHolder.getContext().getAuthentication();
		SecurityContext securityContextUser0 = mock(SecurityContext.class);
		when(securityContextUser0.getAuthentication()).thenReturn(userAuthentication);
		SecurityContext securityContextUser1 = mock(SecurityContext.class);
		when(securityContextUser1.getAuthentication()).thenReturn(userAuthentication);
		SecurityContext securityContextOtherUser = mock(SecurityContext.class);
		Authentication otherUserAuthentication = when(mock(Authentication.class).getPrincipal()).thenReturn("otherUser")
																								.getMock();
		when(securityContextOtherUser.getAuthentication()).thenReturn(otherUserAuthentication);

		when(securityContextRegistry.getSecurityContexts()).thenReturn(
				Stream.of(securityContextUser0, securityContextUser1, securityContextOtherUser));
		Object user = when(mock(User.class).getUsername()).thenReturn("user").getMock();
		assertEquals(principalSecurityContextRegistryImpl.getSecurityContexts(user).collect(toSet()),
				new HashSet<>(asList(securityContextUser0, securityContextUser1)));
	}

	@Configuration
	public static class Config
	{
		public Config()
		{
			MockitoAnnotations.initMocks(this);
		}

		@Mock
		private SecurityContextRegistry securityContextRegistry;

		@Bean
		public SecurityContextRegistry securityContextRegistry()
		{
			return securityContextRegistry;
		}

		void resetMocks()
		{
			reset(securityContextRegistry);
		}
	}
}