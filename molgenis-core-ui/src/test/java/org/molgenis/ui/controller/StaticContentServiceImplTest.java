package org.molgenis.ui.controller;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.molgenis.data.DataService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.user.MolgenisUserDetailsService;
import org.molgenis.ui.settings.StaticContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration
public class StaticContentServiceImplTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private StaticContentService staticContentService;

	@Test
	public void getContent()
	{
		assertEquals(this.staticContentService.getContent("home"), "<p>Welcome to Molgenis!</p>");
	}

	@Test
	public void isCurrentUserCanEdit_SuperUser()
	{
		this.setSecurityContextSuperUser();
		assertTrue(this.staticContentService.isCurrentUserCanEdit());
	}

	@Test
	public void isCurrentUserCanEdit_NonSuperUser()
	{
		this.setSecurityContextNonSuperUser();
		assertFalse(this.staticContentService.isCurrentUserCanEdit());
	}

	@Test
	public void isCurrentUserCanEdit_AnonymousUsers()
	{
		this.setSecurityContextAnonymousUsers();
		assertFalse(this.staticContentService.isCurrentUserCanEdit());
	}

	@Test
	public void submitContent()
	{
		assertTrue(this.staticContentService.submitContent("home", "<p>Welcome to Molgenis!</p>"));
	}

	private void setSecurityContextSuperUser()
	{
		Collection<? extends GrantedAuthority> authorities = Arrays
				.<SimpleGrantedAuthority> asList(new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_SU));

		Authentication authentication = mock(Authentication.class);

		doReturn(authorities).when(authentication).getAuthorities();

		when(authentication.isAuthenticated()).thenReturn(true);
		UserDetails userDetails = when(mock(UserDetails.class).getUsername()).thenReturn(SecurityUtils.AUTHORITY_SU)
				.getMock();
		when(authentication.getPrincipal()).thenReturn(userDetails);

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private void setSecurityContextNonSuperUser()
	{
		Collection<? extends GrantedAuthority> authorities = Arrays.<SimpleGrantedAuthority> asList(
				new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX + "HOME"),
				new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_PLUGIN_WRITE_PREFIX + "HOME"));

		Authentication authentication = mock(Authentication.class);

		doReturn(authorities).when(authentication).getAuthorities();

		when(authentication.isAuthenticated()).thenReturn(true);
		UserDetails userDetails = when(mock(UserDetails.class).getUsername()).thenReturn("user").getMock();
		when(authentication.getPrincipal()).thenReturn(userDetails);

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private void setSecurityContextAnonymousUsers()
	{
		Authentication authentication = mock(Authentication.class);

		when(authentication.isAuthenticated()).thenReturn(false);
		UserDetails userDetails = when(mock(UserDetails.class).getUsername())
				.thenReturn(SecurityUtils.ANONYMOUS_USERNAME).getMock();
		when(authentication.getPrincipal()).thenReturn(userDetails);

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Configuration
	@EnableWebSecurity
	@EnableGlobalMethodSecurity(prePostEnabled = true)
	public static class Config extends WebSecurityConfigurerAdapter
	{
		@Bean
		public StaticContentService staticContentService()
		{
			return new StaticContentServiceImpl(dataService());
		}

		@Bean
		public DataService dataService()
		{
			DataService dataService = mock(DataService.class);
			StaticContent staticContent = when(mock(StaticContent.class).getContent())
					.thenReturn("<p>Welcome to Molgenis!</p>").getMock();
			when(dataService.findOne(StaticContent.ENTITY_NAME, "home", StaticContent.class)).thenReturn(staticContent);
			return dataService;
		}

		@Override
		protected UserDetailsService userDetailsService()
		{
			return mock(MolgenisUserDetailsService.class);
		}

		@Bean
		@Override
		public UserDetailsService userDetailsServiceBean() throws Exception
		{
			return userDetailsService();
		}

		@Bean
		@Override
		public AuthenticationManager authenticationManagerBean() throws Exception
		{
			return super.authenticationManagerBean();
		}

		@Autowired
		@Override
		public void configure(AuthenticationManagerBuilder auth) throws Exception
		{
			auth.inMemoryAuthentication().withUser("user").password("password").authorities("ROLE_USER");
		}
	}
}