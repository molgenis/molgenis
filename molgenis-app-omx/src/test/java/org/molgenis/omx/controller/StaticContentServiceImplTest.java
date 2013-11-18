package org.molgenis.omx.controller;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.security.SecurityUtils;
import org.molgenis.security.user.MolgenisUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
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

	@Autowired
	private Database database;
	
	@Test
	public void getContent()
	{
		assertEquals(this.staticContentService.getContent("home"), "<p>Welcome to Molgenis!</p>");
	}
	
	@Test
	public void isCurrentUserAuthenticatedSu_SuperUser()
	{
		this.setSecurityContextSuperUser();
		assertTrue(this.staticContentService.isCurrentUserAuthenticatedSu());
	}
	
	@Test
	public void isCurrentUserAuthenticatedSu_NonSuperUser()
	{
		this.setSecurityContextNonSuperUser();
		assertFalse(this.staticContentService.isCurrentUserAuthenticatedSu());
	}
	
	@Test
	public void isCurrentUserAuthenticatedSu_AnonymousUsers()
	{
		this.setSecurityContextAnonymousUsers();
		assertFalse(this.staticContentService.isCurrentUserAuthenticatedSu());
	}
	
	@Test
	public void submitContent()
	{	
		assertTrue(this.staticContentService.submitContent("home", StaticContentServiceImpl.DEFAULT_CONTENT));
	}
	
	private void setSecurityContextSuperUser()
	{
		Collection<? extends GrantedAuthority> authorities = Arrays
				.<SimpleGrantedAuthority> asList(new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_SU));
		
		Authentication authentication = mock(Authentication.class);
		
		doReturn(authorities).when(authentication).getAuthorities();
		
		when(authentication.isAuthenticated()).thenReturn(true);
		UserDetails userDetails = when(mock(UserDetails.class).getUsername()).thenReturn(SecurityUtils.AUTHORITY_SU).getMock();
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
		UserDetails userDetails = when(mock(UserDetails.class).getUsername()).thenReturn(SecurityUtils.ANONYMOUS_USERNAME).getMock();
		when(authentication.getPrincipal()).thenReturn(userDetails);
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
	
	
	@Configuration
	@EnableWebSecurity
	@EnableGlobalMethodSecurity(prePostEnabled = true)
	public static class Config extends WebSecurityConfigurerAdapter
	{
		@Bean
		public Database database() throws DatabaseException
		{
			return mock(Database.class);
		}
		
		@Bean
		public StaticContentService staticContentService(){		
			MolgenisSettings molgenisSettings = mock(MolgenisSettings.class);
			when(molgenisSettings.getProperty(StaticContentServiceImpl.PREFIX_KEY + "home" , StaticContentServiceImpl.DEFAULT_CONTENT))
				.thenReturn("<p>Welcome to Molgenis!</p>");		
			
			when(molgenisSettings.propertyExists(StaticContentServiceImpl.PREFIX_KEY + "home"))
				.thenReturn(true);		
			
			when(molgenisSettings.updateProperty(StaticContentServiceImpl.PREFIX_KEY + "home" , StaticContentServiceImpl.DEFAULT_CONTENT))
				.thenReturn(true);
			
			StaticContentService staticContentService = new StaticContentServiceImpl(molgenisSettings);
			return staticContentService;
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
	}
}
