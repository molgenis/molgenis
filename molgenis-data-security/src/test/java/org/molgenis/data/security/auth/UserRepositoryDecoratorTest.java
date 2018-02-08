package org.molgenis.data.security.auth;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.security.auth.GroupMemberMetaData.GROUP_MEMBER;

public class UserRepositoryDecoratorTest extends AbstractMockitoTest
{
	@Mock
	private Repository<User> delegateRepository;
	@Mock
	private DataService dataService;
	@Mock
	private PasswordEncoder passwordEncoder;

	private UserRepositoryDecorator userRepositoryDecorator;

	@BeforeMethod
	public void setUp()
	{
		userRepositoryDecorator = new UserRepositoryDecorator(delegateRepository, dataService, passwordEncoder);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testUserRepositoryDecorator()
	{
		new UserRepositoryDecorator(null, null, null);
	}

	@Test
	public void addEntity()
	{
		String password = "password";
		User user = mock(User.class);
		when(user.getPassword()).thenReturn(password);
		userRepositoryDecorator.add(user);
		verify(passwordEncoder).encode(password);
		verify(delegateRepository).add(user);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void addStream()
	{
		String password = "password";
		User user0 = mock(User.class);
		when(user0.getPassword()).thenReturn(password);
		User user1 = mock(User.class);
		when(user1.getPassword()).thenReturn(password);

		when(delegateRepository.add(any(Stream.class))).thenAnswer(invocation ->
		{
			Stream<Entity> entities = (Stream<Entity>) invocation.getArguments()[0];
			List<Entity> entitiesList = entities.collect(toList());
			return entitiesList.size();
		});
		Assert.assertEquals(userRepositoryDecorator.add(Stream.of(user0, user1)), Integer.valueOf(2));
		verify(passwordEncoder, times(2)).encode(password);
	}

	@Test
	public void delete()
	{
		User user = mock(User.class);

		Stream<GroupMember> groupMembers = Stream.of(mock(GroupMember.class));
		when(dataService.findAll(GROUP_MEMBER, new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, user),
				GroupMember.class)).thenReturn(groupMembers);

		userRepositoryDecorator.delete(user);

		verify(delegateRepository, times(1)).delete(user);
		verify(dataService, times(1)).delete(GROUP_MEMBER, groupMembers);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void deleteStream()
	{
		User user = mock(User.class);

		Stream<User> entities = Stream.of(user);
		Stream<GroupMember> groupMembers = Stream.of(mock(GroupMember.class));
		when(dataService.findAll(GROUP_MEMBER, new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, user),
				GroupMember.class)).thenReturn(groupMembers);

		ArgumentCaptor<Stream> captor = ArgumentCaptor.forClass(Stream.class);
		userRepositoryDecorator.delete(entities);

		verify(delegateRepository, times(1)).delete(captor.capture());
		captor.getValue().forEach(u ->
		{
		});
		verify(dataService, times(1)).delete(GROUP_MEMBER, groupMembers);
	}

	@Test
	public void deleteById()
	{
		User user = mock(User.class);

		Stream<GroupMember> groupMembers = Stream.of(mock(GroupMember.class));
		when(dataService.findAll(GROUP_MEMBER, new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, user),
				GroupMember.class)).thenReturn(groupMembers);

		userRepositoryDecorator.delete(user);

		verify(delegateRepository, times(1)).delete(user);
		verify(dataService, times(1)).delete(GROUP_MEMBER, groupMembers);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void deleteAllStream()
	{
		User user = mock(User.class);
		when(userRepositoryDecorator.findOneById("1")).thenReturn(user);

		Stream<GroupMember> groupMembers = Stream.of(mock(GroupMember.class));
		when(dataService.findAll(GROUP_MEMBER, new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, user),
				GroupMember.class)).thenReturn(groupMembers);

		ArgumentCaptor<Stream> captor = ArgumentCaptor.forClass(Stream.class);
		userRepositoryDecorator.deleteAll(Stream.of("1"));

		verify(delegateRepository, times(1)).deleteAll(captor.capture());
		captor.getValue().forEach(u ->
		{
		});
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
		when(currentUser.getPassword()).thenReturn("currentPasswordHash");
		when(userRepositoryDecorator.findOneById("1")).thenReturn(currentUser);

		User user = mock(User.class);
		when(user.getId()).thenReturn("1");
		when(user.getPassword()).thenReturn("password");

		Stream<User> entities = Stream.of(user);
		ArgumentCaptor<Stream<User>> captor = ArgumentCaptor.forClass(Stream.class);
		doNothing().when(delegateRepository).update(captor.capture());
		userRepositoryDecorator.update(entities);
		Assert.assertEquals(captor.getValue().collect(toList()), singletonList(user));
		verify(user).setPassword("passwordHash");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void updateStreamUnchangedPassword()
	{
		User currentUser = mock(User.class);
		when(currentUser.getPassword()).thenReturn("currentPasswordHash");
		when(userRepositoryDecorator.findOneById("1")).thenReturn(currentUser);

		User user = mock(User.class);
		when(user.getId()).thenReturn("1");
		when(user.getPassword()).thenReturn("currentPasswordHash");

		Stream<User> entities = Stream.of(user);
		ArgumentCaptor<Stream<User>> captor = ArgumentCaptor.forClass(Stream.class);
		doNothing().when(delegateRepository).update(captor.capture());
		userRepositoryDecorator.update(entities);
		Assert.assertEquals(captor.getValue().collect(toList()), singletonList(user));
		verify(user).setPassword("currentPasswordHash");
	}
}
