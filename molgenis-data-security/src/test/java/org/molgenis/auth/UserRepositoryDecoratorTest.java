package org.molgenis.auth;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.support.QueryImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.molgenis.auth.GroupMemberMetaData.GROUP_MEMBER;
import static org.molgenis.auth.UserAuthorityMetaData.USER_AUTHORITY;

public class UserRepositoryDecoratorTest
{
	private Repository<User> decoratedRepository;
	private Repository<UserAuthority> userAuthorityRepository;
	private Repository<GroupMember> groupMemberRepository;
	private UserRepositoryDecorator userRepositoryDecorator;
	private PasswordEncoder passwordEncoder;
	private DataService dataService;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUp()
	{
		decoratedRepository = mock(Repository.class);
		userAuthorityRepository = mock(Repository.class);
		groupMemberRepository = mock(Repository.class);
		UserAuthorityFactory userAuthorityFactory = mock(UserAuthorityFactory.class);
		when(userAuthorityFactory.create()).thenAnswer(invocation -> mock(UserAuthority.class));
		dataService = mock(DataService.class);
		when(dataService.getRepository(USER_AUTHORITY, UserAuthority.class)).thenReturn(userAuthorityRepository);
		when(dataService.getRepository(GROUP_MEMBER, GroupMember.class)).thenReturn(groupMemberRepository);
		passwordEncoder = mock(PasswordEncoder.class);
		userRepositoryDecorator = new UserRepositoryDecorator(decoratedRepository, userAuthorityFactory, dataService,
				passwordEncoder);
	}

	@Test
	public void testDelegate() throws Exception
	{
		Assert.assertEquals(userRepositoryDecorator.delegate(), decoratedRepository);
	}

	@Test
	public void addEntity()
	{
		String password = "password";
		User user = mock(User.class);
		when(user.getPassword()).thenReturn(password);
		when(user.isSuperuser()).thenReturn(false);
		userRepositoryDecorator.add(user);
		verify(passwordEncoder).encode(password);
		verify(decoratedRepository).add(user);
		verify(userAuthorityRepository, times(0)).add(any(UserAuthority.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void addStream()
	{
		String password = "password";
		User user0 = mock(User.class);
		when(user0.getPassword()).thenReturn(password);
		when(user0.isSuperuser()).thenReturn(false);
		User user1 = mock(User.class);
		when(user1.getPassword()).thenReturn(password);
		when(user1.isSuperuser()).thenReturn(false);

		when(decoratedRepository.add(any(Stream.class))).thenAnswer(invocation ->
		{
			Stream<Entity> entities = (Stream<Entity>) invocation.getArguments()[0];
			List<Entity> entitiesList = entities.collect(toList());
			return entitiesList.size();
		});
		Assert.assertEquals(userRepositoryDecorator.add(Stream.of(user0, user1)), Integer.valueOf(2));
		verify(passwordEncoder, times(2)).encode(password);
		verify(userAuthorityRepository, times(0)).add(any(UserAuthority.class));
	}

	@Test
	public void delete()
	{
		User user = mock(User.class);

		Stream<GroupMember> groupMembers = Stream.of(mock(GroupMember.class));
		Stream<UserAuthority> userAuthorities = Stream.of(mock(UserAuthority.class));
		when(dataService.findAll(USER_AUTHORITY, new QueryImpl<UserAuthority>().eq(UserAuthorityMetaData.USER, user),
				UserAuthority.class)).thenReturn(userAuthorities);
		when(dataService.findAll(GROUP_MEMBER, new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, user),
				GroupMember.class)).thenReturn(groupMembers);

		userRepositoryDecorator.delete(user);

		verify(decoratedRepository, times(1)).delete(user);
		verify(dataService, times(1)).delete(USER_AUTHORITY, userAuthorities);
		verify(dataService, times(1)).delete(GROUP_MEMBER, groupMembers);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void deleteStream()
	{
		User user = mock(User.class);

		Stream<User> entities = Stream.of(user);
		Stream<GroupMember> groupMembers = Stream.of(mock(GroupMember.class));
		Stream<UserAuthority> userAuthorities = Stream.of(mock(UserAuthority.class));
		when(dataService.findAll(USER_AUTHORITY, new QueryImpl<UserAuthority>().eq(UserAuthorityMetaData.USER, user),
				UserAuthority.class)).thenReturn(userAuthorities);
		when(dataService.findAll(GROUP_MEMBER, new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, user),
				GroupMember.class)).thenReturn(groupMembers);

		ArgumentCaptor<Stream> captor = ArgumentCaptor.forClass(Stream.class);
		userRepositoryDecorator.delete(entities);

		verify(decoratedRepository, times(1)).delete(captor.capture());
		captor.getValue().forEach(u ->
		{
		});
		verify(dataService, times(1)).delete(USER_AUTHORITY, userAuthorities);
		verify(dataService, times(1)).delete(GROUP_MEMBER, groupMembers);
	}

	@Test
	public void deleteById()
	{
		User user = mock(User.class);
		when(userRepositoryDecorator.findOneById("1")).thenReturn(user);

		Stream<GroupMember> groupMembers = Stream.of(mock(GroupMember.class));
		Stream<UserAuthority> userAuthorities = Stream.of(mock(UserAuthority.class));
		when(dataService.findAll(USER_AUTHORITY, new QueryImpl<UserAuthority>().eq(UserAuthorityMetaData.USER, user),
				UserAuthority.class)).thenReturn(userAuthorities);
		when(dataService.findAll(GROUP_MEMBER, new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, user),
				GroupMember.class)).thenReturn(groupMembers);

		userRepositoryDecorator.delete(user);

		verify(decoratedRepository, times(1)).delete(user);
		verify(dataService, times(1)).delete(USER_AUTHORITY, userAuthorities);
		verify(dataService, times(1)).delete(GROUP_MEMBER, groupMembers);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void deleteAllStream()
	{
		User user = mock(User.class);
		when(userRepositoryDecorator.findOneById("1")).thenReturn(user);

		Stream<GroupMember> groupMembers = Stream.of(mock(GroupMember.class));
		Stream<UserAuthority> userAuthorities = Stream.of(mock(UserAuthority.class));
		when(dataService.findAll(USER_AUTHORITY, new QueryImpl<UserAuthority>().eq(UserAuthorityMetaData.USER, user),
				UserAuthority.class)).thenReturn(userAuthorities);
		when(dataService.findAll(GROUP_MEMBER, new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, user),
				GroupMember.class)).thenReturn(groupMembers);

		ArgumentCaptor<Stream> captor = ArgumentCaptor.forClass(Stream.class);
		userRepositoryDecorator.deleteAll(Stream.of("1"));

		verify(decoratedRepository, times(1)).deleteAll(captor.capture());
		captor.getValue().forEach(u ->
		{
		});
		verify(dataService, times(1)).delete(USER_AUTHORITY, userAuthorities);
		verify(dataService, times(1)).delete(GROUP_MEMBER, groupMembers);
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void deleteAll()
	{
		userRepositoryDecorator.deleteAll();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void updateStream()
	{
		when(passwordEncoder.encode("password")).thenReturn("passwordHash");

		User currentUser = mock(User.class);
		when(currentUser.getId()).thenReturn("1");
		when(currentUser.getPassword()).thenReturn("currentPasswordHash");
		when(userRepositoryDecorator.findOneById("1")).thenReturn(currentUser);

		User user = mock(User.class);
		when(user.getId()).thenReturn("1");
		when(user.getPassword()).thenReturn("password");

		Stream<User> entities = Stream.of(user);
		ArgumentCaptor<Stream<User>> captor = ArgumentCaptor.forClass(Stream.class);
		doNothing().when(decoratedRepository).update(captor.capture());
		userRepositoryDecorator.update(entities);
		Assert.assertEquals(captor.getValue().collect(toList()), singletonList(user));
		verify(user).setPassword("passwordHash");
		// TODO add authority tests
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void updateStreamUnchangedPassword()
	{
		when(passwordEncoder.encode("currentPasswordHash")).thenReturn("blaat");

		User currentUser = mock(User.class);
		when(currentUser.getId()).thenReturn("1");
		when(currentUser.getPassword()).thenReturn("currentPasswordHash");
		when(userRepositoryDecorator.findOneById("1")).thenReturn(currentUser);

		User user = mock(User.class);
		when(user.getId()).thenReturn("1");
		when(user.getPassword()).thenReturn("currentPasswordHash");

		Stream<User> entities = Stream.of(user);
		ArgumentCaptor<Stream<User>> captor = ArgumentCaptor.forClass(Stream.class);
		doNothing().when(decoratedRepository).update(captor.capture());
		userRepositoryDecorator.update(entities);
		Assert.assertEquals(captor.getValue().collect(toList()), singletonList(user));
		verify(user).setPassword("currentPasswordHash");
		// TODO add authority tests
	}

	@Test
	public void addEntitySu()
	{
		String password = "password";
		User user = mock(User.class);
		when(user.getId()).thenReturn("1");
		when(user.getPassword()).thenReturn(password);
		when(user.isSuperuser()).thenReturn(true);
		when(decoratedRepository.findOneById("1")).thenReturn(user);

		userRepositoryDecorator.add(user);
		verify(passwordEncoder).encode(password);
		verify(decoratedRepository).add(user);
	}
}
