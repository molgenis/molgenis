package org.molgenis.data;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.molgenis.data.settings.AppSettings;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RepositorySecurityDecoratorTest
{
	@BeforeMethod
	public void beforeMethod()
	{
		Collection<? extends GrantedAuthority> authorities = Arrays
				.<SimpleGrantedAuthority> asList(new SimpleGrantedAuthority("ROLE_ENTITY_READ_MYENTITY"));

		Authentication authentication = mock(Authentication.class);

		doReturn(authorities).when(authentication).getAuthorities();

		when(authentication.isAuthenticated()).thenReturn(true);
		UserDetails userDetails = when(mock(UserDetails.class).getUsername()).thenReturn("user").getMock();
		when(authentication.getPrincipal()).thenReturn(userDetails);

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Test
	public void addEntityListener() throws IOException
	{
		Repository repo = when(mock(Repository.class).getName()).thenReturn("myentity").getMock();

		@SuppressWarnings("resource")
		RepositorySecurityDecorator repoSecurityDecorator = new RepositorySecurityDecorator(repo,
				mock(AppSettings.class));
		repoSecurityDecorator.addEntityListener(mock(EntityListener.class));
	}

	@Test
	public void removeEntityListener()
	{
		Repository repo = when(mock(Repository.class).getName()).thenReturn("myentity").getMock();

		@SuppressWarnings("resource")
		RepositorySecurityDecorator repoSecurityDecorator = new RepositorySecurityDecorator(repo,
				mock(AppSettings.class));
		repoSecurityDecorator.removeEntityListener(mock(EntityListener.class));
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void addEntityListenerNotAllowed() throws IOException
	{
		Repository repo = when(mock(Repository.class).getName()).thenReturn("yourentity").getMock();

		@SuppressWarnings("resource")
		RepositorySecurityDecorator repoSecurityDecorator = new RepositorySecurityDecorator(repo,
				mock(AppSettings.class));
		repoSecurityDecorator.addEntityListener(mock(EntityListener.class));
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void removeEntityListenerNotAllowed()
	{
		Repository repo = when(mock(Repository.class).getName()).thenReturn("yourentity").getMock();

		@SuppressWarnings("resource")
		RepositorySecurityDecorator repoSecurityDecorator = new RepositorySecurityDecorator(repo,
				mock(AppSettings.class));
		repoSecurityDecorator.removeEntityListener(mock(EntityListener.class));
	}
}
