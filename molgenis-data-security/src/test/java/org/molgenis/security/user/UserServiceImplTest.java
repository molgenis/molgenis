package org.molgenis.security.user;

import com.google.common.collect.ImmutableList;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.DataService;
import org.molgenis.data.security.model.UserEntity;
import org.molgenis.data.security.model.UserFactory;
import org.molgenis.data.security.model.UserMetadata;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.EmailAlreadyExistsException;
import org.molgenis.security.core.service.UsernameAlreadyExistsException;
import org.molgenis.security.user.UserServiceImplTest.Config;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.molgenis.data.security.model.UserMetadata.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

@ContextConfiguration(classes = { Config.class })
@TestExecutionListeners(WithSecurityContextTestExecutionListener.class)
public class UserServiceImplTest extends AbstractMockitoTestNGSpringContextTests
{
	@Autowired
	private UserServiceImpl userService;

	@Autowired
	private DataService dataService;

	@Autowired
	private UserFactory userFactory;

	@Mock
	private UserEntity userEntity;

	@Mock
	private UserEntity userEntity2;

	@Mock
	private User user;

	@Mock
	private User user2;

	@BeforeMethod
	public void beforeMethod()
	{
		MockitoAnnotations.initMocks(this);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void MolgenisUserServiceImpl()
	{
		new UserServiceImpl(null, null);
	}

	@Test
	public void testFindByUsernameFound()
	{
		when(userEntity.toUser()).thenReturn(user);

		when(dataService.findOne(USER, new QueryImpl<UserEntity>().eq(USERNAME, "abc"), UserEntity.class)).thenReturn(
				userEntity);

		assertEquals(userService.findByUsername("abc"), user);
	}

	@Test
	public void testAdd() throws EmailAlreadyExistsException, UsernameAlreadyExistsException
	{
		when(userFactory.create()).thenReturn(userEntity);
		when(userEntity.updateFrom(user)).thenReturn(userEntity);

		assertSame(userService.add(user), user);

		verify(userEntity).updateFrom(user);
		verify(dataService).add(USER, userEntity);
	}

	@Test
	public void testGetSuEmailAddresses()
	{
		when(dataService.findAll(USER, new QueryImpl<UserEntity>().eq(UserMetadata.SUPERUSER, true),
				UserEntity.class)).thenReturn(Stream.of(userEntity, userEntity2));
		when(userEntity.getEmail()).thenReturn("email1@example.com");
		when(userEntity2.getEmail()).thenReturn("email2@example.com");

		assertEquals(userService.getSuEmailAddresses(), Arrays.asList("email1@example.com", "email2@example.com"));
	}

	@Test
	public void testFindByEmailFound()
	{
		when(dataService.findOne(USER, new QueryImpl<UserEntity>().eq(EMAIL, "email@example.com"),
				UserEntity.class)).thenReturn(userEntity);
		when(userEntity.toUser()).thenReturn(user);

		assertEquals(userService.findByEmail("email@example.com"), user);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testFindByEmailNotFound()
	{
		when(dataService.findOne(USER, new QueryImpl<UserEntity>().eq(EMAIL, "email@example.com"),
				UserEntity.class)).thenReturn(null);

		userService.findByEmail("email@example.com");
	}

	@Test
	public void testFindByGoogleIdIfPresent()
	{
		when(dataService.findOne(USER, new QueryImpl<UserEntity>().eq(GOOGLEACCOUNTID, "email@google.com"),
				UserEntity.class)).thenReturn(userEntity);
		when(userEntity.toUser()).thenReturn(user);

		assertEquals(userService.findByGoogleAccountIdIfPresent("email@google.com"), Optional.of(user));
	}

	@Test
	public void testFindByGoogleIdNotFound()
	{
		when(dataService.findOne(USER, new QueryImpl<UserEntity>().eq(GOOGLEACCOUNTID, "email@google.com"),
				UserEntity.class)).thenReturn(null);

		assertEquals(userService.findByGoogleAccountIdIfPresent("email@google.com"), Optional.empty());
	}

	@Test
	public void testUpdate()
	{
		when(user.getUsername()).thenReturn("user");

		when(dataService.findOne(USER, new QueryImpl<UserEntity>().eq(USERNAME, "user"), UserEntity.class)).thenReturn(
				userEntity);
		when(userEntity.updateFrom(user)).thenReturn(userEntity);

		assertSame(userService.update(user), user);

		verify(dataService).update(USER, userEntity);
		verify(userEntity).updateFrom(user);
	}

	@Test
	public void testGetAllUsers()
	{
		when(dataService.findAll(USER, UserEntity.class)).thenReturn(Stream.of(userEntity, userEntity2));
		when(userEntity.toUser()).thenReturn(user);
		when(userEntity2.toUser()).thenReturn(user2);

		assertEquals(userService.getAllUsers(), ImmutableList.of(user, user2));
	}

	@Configuration
	public static class Config
	{
		@Bean
		public UserServiceImpl molgenisUserServiceImpl()
		{
			return new UserServiceImpl(dataService(), userFactory());
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public UserFactory userFactory()
		{
			return mock(UserFactory.class);
		}
	}
}
