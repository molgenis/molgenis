package org.molgenis.data.security.model;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;

public class UserRepositoryDecoratorTest
{
	private Repository<UserEntity> delegateRepository;
	private UserRepositoryDecorator userRepositoryDecorator;
	private PasswordEncoder passwordEncoder;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUp()
	{
		delegateRepository = mock(Repository.class);
		passwordEncoder = mock(PasswordEncoder.class);
		userRepositoryDecorator = new UserRepositoryDecorator(delegateRepository,
				passwordEncoder);
	}

	@Test
	public void addEntity()
	{
		String password = "password";
		UserEntity user = mock(UserEntity.class);
		when(user.getPassword()).thenReturn(password);
		when(user.isSuperuser()).thenReturn(false);
		userRepositoryDecorator.add(user);
		verify(passwordEncoder).encode(password);
		verify(delegateRepository).add(user);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void addStream()
	{
		String password = "password";
		UserEntity user0 = mock(UserEntity.class);
		when(user0.getPassword()).thenReturn(password);
		when(user0.isSuperuser()).thenReturn(false);
		UserEntity user1 = mock(UserEntity.class);
		when(user1.getPassword()).thenReturn(password);
		when(user1.isSuperuser()).thenReturn(false);

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
		UserEntity user = mock(UserEntity.class);

		Stream<GroupMembershipEntity> groupMembers = Stream.of(mock(GroupMembershipEntity.class));

		userRepositoryDecorator.delete(user);

		verify(delegateRepository, times(1)).delete(user);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void deleteStream()
	{
		UserEntity user = mock(UserEntity.class);

		Stream<UserEntity> entities = Stream.of(user);
		Stream<GroupMembershipEntity> groupMembers = Stream.of(mock(GroupMembershipEntity.class));

		ArgumentCaptor<Stream> captor = ArgumentCaptor.forClass(Stream.class);
		userRepositoryDecorator.delete(entities);

		verify(delegateRepository, times(1)).delete(captor.capture());
		captor.getValue().forEach(u ->
		{
		});
	}

	@Test
	public void deleteById()
	{
		UserEntity user = mock(UserEntity.class);
		when(userRepositoryDecorator.findOneById("1")).thenReturn(user);

		Stream<GroupMembershipEntity> groupMembers = Stream.of(mock(GroupMembershipEntity.class));

		userRepositoryDecorator.delete(user);

		verify(delegateRepository, times(1)).delete(user);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void deleteAllStream()
	{
		UserEntity user = mock(UserEntity.class);
		when(userRepositoryDecorator.findOneById("1")).thenReturn(user);

		Stream<GroupMembershipEntity> groupMembers = Stream.of(mock(GroupMembershipEntity.class));

		ArgumentCaptor<Stream> captor = ArgumentCaptor.forClass(Stream.class);
		userRepositoryDecorator.deleteAll(Stream.of("1"));

		verify(delegateRepository, times(1)).deleteAll(captor.capture());
		captor.getValue().forEach(u ->
		{
		});
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

		UserEntity currentUser = mock(UserEntity.class);
		when(currentUser.getId()).thenReturn("1");
		when(currentUser.getPassword()).thenReturn("currentPasswordHash");
		when(userRepositoryDecorator.findOneById("1")).thenReturn(currentUser);

		UserEntity user = mock(UserEntity.class);
		when(user.getId()).thenReturn("1");
		when(user.getPassword()).thenReturn("password");

		Stream<UserEntity> entities = Stream.of(user);
		ArgumentCaptor<Stream<UserEntity>> captor = ArgumentCaptor.forClass(Stream.class);
		doNothing().when(delegateRepository).update(captor.capture());
		userRepositoryDecorator.update(entities);
		Assert.assertEquals(captor.getValue().collect(toList()), singletonList(user));
		verify(user).setPassword("passwordHash");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void updateStreamUnchangedPassword()
	{
		when(passwordEncoder.encode("currentPasswordHash")).thenReturn("blaat");

		UserEntity currentUser = mock(UserEntity.class);
		when(currentUser.getId()).thenReturn("1");
		when(currentUser.getPassword()).thenReturn("currentPasswordHash");
		when(userRepositoryDecorator.findOneById("1")).thenReturn(currentUser);

		UserEntity user = mock(UserEntity.class);
		when(user.getId()).thenReturn("1");
		when(user.getPassword()).thenReturn("currentPasswordHash");

		Stream<UserEntity> entities = Stream.of(user);
		ArgumentCaptor<Stream<UserEntity>> captor = ArgumentCaptor.forClass(Stream.class);
		doNothing().when(delegateRepository).update(captor.capture());
		userRepositoryDecorator.update(entities);
		Assert.assertEquals(captor.getValue().collect(toList()), singletonList(user));
		verify(user).setPassword("currentPasswordHash");
	}

	@Test
	public void addEntitySu()
	{
		String password = "password";
		UserEntity user = mock(UserEntity.class);
		when(user.getId()).thenReturn("1");
		when(user.getPassword()).thenReturn(password);
		when(user.isSuperuser()).thenReturn(true);
		when(delegateRepository.findOneById("1")).thenReturn(user);

		userRepositoryDecorator.add(user);
		verify(passwordEncoder).encode(password);
		verify(delegateRepository).add(user);
	}
}
