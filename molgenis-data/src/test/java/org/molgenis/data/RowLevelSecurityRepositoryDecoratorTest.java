package org.molgenis.data;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.molgenis.auth.MolgenisUser.USERNAME;
import static org.molgenis.data.RowLevelSecurityRepositoryDecorator.PERMISSIONS_ATTRIBUTE;
import static org.molgenis.data.RowLevelSecurityRepositoryDecorator.UPDATE_ATTRIBUTE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.QueryImpl;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RowLevelSecurityRepositoryDecoratorTest
{
	private String entityName;
	private EntityMetaData entityMetaData;
	private Repository decoratedRepository;
	private AttributeMetaData updatePermissionAttribute;
	private RowLevelSecurityRepositoryDecorator repositoryDecorator;
	private Entity user1;
	private Entity user2;
	private Entity entity1;
	private Entity entity2;
	private Entity entity3;
	private List<Entity> usersEntity1;
	private List<Entity> usersEntity2;
	private List<Entity> usersEntity3;
	private TestingAuthenticationToken adminAuthentication;
	private TestingAuthenticationToken systemAuthentication;
	private TestingAuthenticationToken user1Authentication;
	private TestingAuthenticationToken user2Authentication;

	private ArgumentCaptor<Entity> entityArgumentCaptor;
	private ArgumentCaptor<String> attributeArgumentCaptor;
	private ArgumentCaptor<String> permissionsArgumentCaptor;
	private ArgumentCaptor<List> usersArgumentCaptor;
	private ArgumentCaptor<Object> idArgumentCaptor;
	private Answer<Void> streamConsumer;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void beforeMethod()
	{
		entityName = "entity";
		entityMetaData = mock(EntityMetaData.class);
		when(entityMetaData.getName()).thenReturn(entityName);

		updatePermissionAttribute = mock(AttributeMetaData.class);
		when(updatePermissionAttribute.getName()).thenReturn(UPDATE_ATTRIBUTE);
		when(updatePermissionAttribute.getDataType()).thenReturn(MolgenisFieldTypes.STRING);

		AttributeMetaData idAttribute = mock(AttributeMetaData.class);
		when(idAttribute.getName()).thenReturn("id");
		when(idAttribute.getDataType()).thenReturn(MolgenisFieldTypes.STRING);

		AttributeMetaData valueAttribute = mock(AttributeMetaData.class);
		when(valueAttribute.getName()).thenReturn("value");
		when(valueAttribute.getDataType()).thenReturn(MolgenisFieldTypes.STRING);

		entityMetaData = mock(EntityMetaData.class);
		when(entityMetaData.getName()).thenReturn(entityName);
		when(entityMetaData.isRowLevelSecured()).thenReturn(true);
		when(entityMetaData.getIdAttribute()).thenReturn(idAttribute);
		when(entityMetaData.getAttribute(UPDATE_ATTRIBUTE)).thenReturn(updatePermissionAttribute);
		when(entityMetaData.getOwnAttributes())
				.thenReturn(newArrayList(idAttribute, valueAttribute, updatePermissionAttribute));
		when(entityMetaData.getAtomicAttributes())
				.thenReturn(newArrayList(idAttribute, valueAttribute, updatePermissionAttribute));
		when(entityMetaData.getOwnIdAttribute()).thenReturn(idAttribute);
		when(entityMetaData.getOwnLabelAttribute()).thenReturn(valueAttribute);
		when(entityMetaData.getOwnLookupAttributes()).thenReturn(newArrayList(valueAttribute));

		user1 = mock(Entity.class);
		when(user1.getString(USERNAME)).thenReturn("user1");
		user2 = mock(Entity.class);
		when(user2.getString(USERNAME)).thenReturn("user2");

		usersEntity1 = newArrayList(user1);
		usersEntity2 = newArrayList(user1, user2);
		usersEntity3 = newArrayList();

		entity1 = mock(Entity.class);
		when(entity1.getIdValue()).thenReturn("entity1");
		when(entity1.getEntityMetaData()).thenReturn(entityMetaData);
		when(entity1.getEntities(UPDATE_ATTRIBUTE)).thenReturn(newArrayList(user1));
		entity2 = mock(Entity.class);
		when(entity2.getIdValue()).thenReturn("entity2");
		when(entity2.getEntityMetaData()).thenReturn(entityMetaData);
		when(entity2.getEntities(UPDATE_ATTRIBUTE)).thenReturn(newArrayList(user1, user2));
		entity3 = mock(Entity.class);
		when(entity3.getIdValue()).thenReturn("entity3");
		when(entity3.getEntityMetaData()).thenReturn(entityMetaData);
		when(entity3.getEntities(UPDATE_ATTRIBUTE)).thenReturn(newArrayList());

		adminAuthentication = new TestingAuthenticationToken("admin", null, "ROLE_SU");
		adminAuthentication.setAuthenticated(false);
		systemAuthentication = new TestingAuthenticationToken("system", null, "ROLE_SYSTEM");
		systemAuthentication.setAuthenticated(false);
		user1Authentication = new TestingAuthenticationToken("user1", null);
		user1Authentication.setAuthenticated(false);
		user2Authentication = new TestingAuthenticationToken("user2", null);
		user2Authentication.setAuthenticated(false);

		decoratedRepository = mock(Repository.class);
		when(decoratedRepository.getName()).thenReturn(entityName);
		when(decoratedRepository.getEntityMetaData()).thenReturn(entityMetaData);
		when(decoratedRepository.stream(any(Fetch.class))).thenReturn(Stream.of(entity1, entity2, entity3));
		when(decoratedRepository.stream()).thenReturn(Stream.of(entity1, entity2, entity3));
		when(decoratedRepository.findOne("entity1")).thenReturn(entity1);
		when(decoratedRepository.findOne("entity2")).thenReturn(entity2);
		when(decoratedRepository.findOne("entity3")).thenReturn(entity3);

		repositoryDecorator = new RowLevelSecurityRepositoryDecorator(decoratedRepository);

		streamConsumer = (invocation) -> {
			Stream<Entity> entities = (Stream<Entity>) invocation.getArguments()[0];
			entities.collect(Collectors.toList());
			return null;
		};

		entityArgumentCaptor = ArgumentCaptor.forClass(Entity.class);
		attributeArgumentCaptor = ArgumentCaptor.forClass(String.class);
		permissionsArgumentCaptor = ArgumentCaptor.forClass(String.class);
		usersArgumentCaptor = ArgumentCaptor.forClass(List.class);
		idArgumentCaptor = ArgumentCaptor.forClass(Object.class);
	}

	@Test
	public void streamAsUser()
	{
		SecurityContextHolder.getContext().setAuthentication(user1Authentication);
		testStream(UPDATE_ATTRIBUTE, UPDATE_ATTRIBUTE, "");
	}

	@Test
	public void streamAsSu()
	{
		SecurityContextHolder.getContext().setAuthentication(adminAuthentication);
		testStream(UPDATE_ATTRIBUTE, UPDATE_ATTRIBUTE, UPDATE_ATTRIBUTE);
	}

	@Test
	public void streamAsSystem()
	{
		SecurityContextHolder.getContext().setAuthentication(systemAuthentication);
		testStream(UPDATE_ATTRIBUTE, UPDATE_ATTRIBUTE, UPDATE_ATTRIBUTE);
	}

	@Test
	public void getEntityMetaDataAsUser()
	{
		SecurityContextHolder.getContext().setAuthentication(user2Authentication);
		assertEquals(repositoryDecorator.getEntityMetaData().getAttribute(UPDATE_ATTRIBUTE), null);
	}

	@Test
	public void getEntityMetaDataAsSu()
	{
		SecurityContextHolder.getContext().setAuthentication(adminAuthentication);
		assertEquals(repositoryDecorator.getEntityMetaData().getAttribute(UPDATE_ATTRIBUTE), updatePermissionAttribute);
	}

	@Test
	public void getEntityMetaDataAsSystem()
	{
		SecurityContextHolder.getContext().setAuthentication(systemAuthentication);
		assertEquals(repositoryDecorator.getEntityMetaData().getAttribute(UPDATE_ATTRIBUTE), updatePermissionAttribute);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void updateEntityNoPermission()
	{
		SecurityContextHolder.getContext().setAuthentication(user1Authentication);

		try
		{
			repositoryDecorator.update(entity3);
		}
		catch (MolgenisDataAccessException e)
		{
			verifyAddUpdateAttribute(entity3, usersEntity3);

			verify(decoratedRepository, times(1)).getEntityMetaData();
			verify(decoratedRepository, never()).update(entity3);
			throw e;
		}
	}

	@Test
	public void updateEntityAsSu()
	{
		SecurityContextHolder.getContext().setAuthentication(adminAuthentication);
		repositoryDecorator.update(entity1);
		verify(decoratedRepository, times(1)).update(entity1);
	}

	@Test
	public void updateEntityAsSystem()
	{
		SecurityContextHolder.getContext().setAuthentication(systemAuthentication);
		repositoryDecorator.update(entity1);
		verify(decoratedRepository, times(1)).update(entity1);
	}

	@Test
	public void updateEntityAsUser()
	{
		SecurityContextHolder.getContext().setAuthentication(user1Authentication);
		repositoryDecorator.update(entity2);
		verify(decoratedRepository, times(1)).update(entityArgumentCaptor.capture());
		assertEquals(entityArgumentCaptor.getValue().getEntityMetaData().getAttribute(UPDATE_ATTRIBUTE),
				updatePermissionAttribute);

		verifyAddUpdateAttribute(entity2, usersEntity2);
	}

	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void updateStreamNoPermission()
	{
		SecurityContextHolder.getContext().setAuthentication(user1Authentication);
		doAnswer(streamConsumer).when(decoratedRepository).update(any(Stream.class));
		Stream stream = Stream.of(entity1, entity2, entity3);
		try
		{
			repositoryDecorator.update(stream);
		}
		catch (MolgenisDataAccessException e)
		{
			verify(decoratedRepository, times(1)).getEntityMetaData();
			verifyAddUpdateAttribute(entity1, usersEntity1);
			verifyAddUpdateAttribute(entity2, usersEntity2);
			verifyAddUpdateAttribute(entity3, usersEntity3);
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void updateStreamAsUser()
	{
		SecurityContextHolder.getContext().setAuthentication(user1Authentication);
		testUpdateStream();
		verifyAddUpdateAttribute(entity1, usersEntity1);
		verifyAddUpdateAttribute(entity2, usersEntity2);
	}

	@Test
	public void updateStreamAsSu()
	{
		SecurityContextHolder.getContext().setAuthentication(adminAuthentication);
		testUpdateStream();
	}

	@Test
	public void updateStreamAsSystem()
	{
		SecurityContextHolder.getContext().setAuthentication(systemAuthentication);
		testUpdateStream();
	}

	@Test
	public void iteratorAsUser()
	{
		SecurityContextHolder.getContext().setAuthentication(user1Authentication);
		testIterator(UPDATE_ATTRIBUTE, UPDATE_ATTRIBUTE, "");
	}

	@Test
	public void iteratorAsSu()
	{
		SecurityContextHolder.getContext().setAuthentication(adminAuthentication);
		testIterator(UPDATE_ATTRIBUTE, UPDATE_ATTRIBUTE, UPDATE_ATTRIBUTE);
	}

	@Test
	public void iteratorAsSystem()
	{
		SecurityContextHolder.getContext().setAuthentication(systemAuthentication);
		testIterator(UPDATE_ATTRIBUTE, UPDATE_ATTRIBUTE, UPDATE_ATTRIBUTE);
	}

	@Test
	public void findAllQueryAsUser()
	{
		SecurityContextHolder.getContext().setAuthentication(user2Authentication);
		testFindAllQuery("", UPDATE_ATTRIBUTE);
	}

	@Test
	public void findAllQueryAsSu()
	{
		SecurityContextHolder.getContext().setAuthentication(adminAuthentication);
		testFindAllQuery(UPDATE_ATTRIBUTE, UPDATE_ATTRIBUTE);
	}

	@Test
	public void findAllQueryAsSystem()
	{
		SecurityContextHolder.getContext().setAuthentication(systemAuthentication);
		testFindAllQuery(UPDATE_ATTRIBUTE, UPDATE_ATTRIBUTE);
	}

	@Test
	public void findOneQueryAsUser()
	{
		SecurityContextHolder.getContext().setAuthentication(user1Authentication);
		testFindOneQuery("");
	}

	@Test
	public void findOneQueryAsSu()
	{
		SecurityContextHolder.getContext().setAuthentication(adminAuthentication);
		testFindOneQuery(UPDATE_ATTRIBUTE);
	}

	@Test
	public void findOneQueryAsSystem()
	{
		SecurityContextHolder.getContext().setAuthentication(systemAuthentication);
		testFindOneQuery(UPDATE_ATTRIBUTE);
	}

	@Test
	public void findOneIdAsUser()
	{
		SecurityContextHolder.getContext().setAuthentication(user1Authentication);
		testFindOneId(UPDATE_ATTRIBUTE);
	}

	@Test
	public void findOneIdAsSu()
	{
		SecurityContextHolder.getContext().setAuthentication(adminAuthentication);
		testFindOneId(UPDATE_ATTRIBUTE);
	}

	@Test
	public void findOneIdAsSystem()
	{
		SecurityContextHolder.getContext().setAuthentication(systemAuthentication);
		testFindOneId(UPDATE_ATTRIBUTE);
	}

	@Test
	public void findOneIdFetchAsUser()
	{
		SecurityContextHolder.getContext().setAuthentication(user1Authentication);
		testFindOneIdFetch(UPDATE_ATTRIBUTE);
	}

	@Test
	public void findOneIdFetchAsSu()
	{
		SecurityContextHolder.getContext().setAuthentication(adminAuthentication);
		testFindOneIdFetch(UPDATE_ATTRIBUTE);
	}

	@Test
	public void findOneIdFetchAsSystem()
	{
		SecurityContextHolder.getContext().setAuthentication(systemAuthentication);
		testFindOneIdFetch(UPDATE_ATTRIBUTE);
	}

	@Test
	public void findAllStreamAsUser()
	{
		SecurityContextHolder.getContext().setAuthentication(user2Authentication);
		testFindAllStream("", UPDATE_ATTRIBUTE);
	}

	@Test
	public void findAllStreamAsSu()
	{
		SecurityContextHolder.getContext().setAuthentication(adminAuthentication);
		testFindAllStream(UPDATE_ATTRIBUTE, UPDATE_ATTRIBUTE);
	}

	@Test
	public void findAllStreamAsSystem()
	{
		SecurityContextHolder.getContext().setAuthentication(systemAuthentication);
		testFindAllStream(UPDATE_ATTRIBUTE, UPDATE_ATTRIBUTE);
	}

	@Test
	public void findAllStreamFetchAsUser()
	{
		SecurityContextHolder.getContext().setAuthentication(user2Authentication);
		testFindAllStreamFetch("", UPDATE_ATTRIBUTE);
	}

	@Test
	public void findAllStreamFetchAsSu()
	{
		SecurityContextHolder.getContext().setAuthentication(adminAuthentication);
		testFindAllStreamFetch(UPDATE_ATTRIBUTE, UPDATE_ATTRIBUTE);
	}

	@Test
	public void findAllStreamFetchAsSystem()
	{
		SecurityContextHolder.getContext().setAuthentication(systemAuthentication);
		testFindAllStreamFetch(UPDATE_ATTRIBUTE, UPDATE_ATTRIBUTE);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void deleteEntityNoPermission()
	{
		SecurityContextHolder.getContext().setAuthentication(user1Authentication);

		try
		{
			repositoryDecorator.delete(entity3);
		}
		catch (MolgenisDataAccessException e)
		{
			verifyAddUpdateAttribute(entity3, usersEntity3);

			verify(decoratedRepository, times(1)).getEntityMetaData();
			verify(decoratedRepository, never()).delete(entity3);
			throw e;
		}
	}

	@Test
	public void deleteEntityAsSu()
	{
		SecurityContextHolder.getContext().setAuthentication(adminAuthentication);
		repositoryDecorator.delete(entity1);
		verify(decoratedRepository, times(1)).delete(entity1);
	}

	@Test
	public void deleteEntityAsSystem()
	{
		SecurityContextHolder.getContext().setAuthentication(systemAuthentication);
		repositoryDecorator.delete(entity1);
		verify(decoratedRepository, times(1)).delete(entity1);
	}

	@Test
	public void deleteEntityAsUser()
	{
		SecurityContextHolder.getContext().setAuthentication(user1Authentication);
		repositoryDecorator.delete(entity2);
		verify(decoratedRepository, times(1)).delete(entityArgumentCaptor.capture());
		assertEquals(entityArgumentCaptor.getValue().getEntityMetaData().getAttribute(UPDATE_ATTRIBUTE),
				updatePermissionAttribute);

		verifyAddUpdateAttribute(entity2, usersEntity2);
	}

	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void deleteStreamNoPermission()
	{
		SecurityContextHolder.getContext().setAuthentication(user1Authentication);
		doAnswer(streamConsumer).when(decoratedRepository).delete(any(Stream.class));
		Stream stream = Stream.of(entity2, entity3);
		try
		{
			repositoryDecorator.delete(stream);
		}
		catch (MolgenisDataAccessException e)
		{
			verify(decoratedRepository, times(1)).getEntityMetaData();
			verifyAddUpdateAttribute(entity2, usersEntity2);
			verifyAddUpdateAttribute(entity3, usersEntity3);
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void deleteStreamAsUser()
	{
		SecurityContextHolder.getContext().setAuthentication(user1Authentication);
		testDeleteStream();
		verifyAddUpdateAttribute(entity1, usersEntity1);
		verifyAddUpdateAttribute(entity2, usersEntity2);
	}

	@Test
	public void deleteStreamAsSu()
	{
		SecurityContextHolder.getContext().setAuthentication(adminAuthentication);
		testDeleteStream();
	}

	@Test
	public void deleteStreamAsSystem()
	{
		SecurityContextHolder.getContext().setAuthentication(systemAuthentication);
		testDeleteStream();
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void deleteByIdNoPermission()
	{
		SecurityContextHolder.getContext().setAuthentication(user1Authentication);

		try
		{
			repositoryDecorator.deleteById("entity3");
		}
		catch (MolgenisDataAccessException e)
		{
			verify(decoratedRepository, times(1)).getEntityMetaData();
			verify(decoratedRepository, never()).deleteById("entity3");
			throw e;
		}
	}

	@Test
	public void deleteByIdAsSu()
	{
		SecurityContextHolder.getContext().setAuthentication(adminAuthentication);
		repositoryDecorator.deleteById("entity1");
		verify(decoratedRepository, times(1)).deleteById("entity1");
	}

	@Test
	public void deleteByIdSystem()
	{
		SecurityContextHolder.getContext().setAuthentication(systemAuthentication);
		repositoryDecorator.deleteById("entity1");
		verify(decoratedRepository, times(1)).deleteById("entity1");
	}

	@Test
	public void deleteByIdUser()
	{
		SecurityContextHolder.getContext().setAuthentication(user1Authentication);
		repositoryDecorator.deleteById("entity2");
		verify(decoratedRepository, times(1)).deleteById(idArgumentCaptor.capture());
		assertEquals(idArgumentCaptor.getValue(), "entity2");
	}

	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void deleteByIdStreamNoPermission()
	{
		SecurityContextHolder.getContext().setAuthentication(user1Authentication);
		doAnswer(streamConsumer).when(decoratedRepository).deleteById(any(Stream.class));
		Stream stream = Stream.of("entity2", "entity3");
		try
		{
			repositoryDecorator.deleteById(stream);
		}
		catch (MolgenisDataAccessException e)
		{
			verify(decoratedRepository, times(1)).getEntityMetaData();
			verify(decoratedRepository, times(1)).deleteById(any(Stream.class));
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void deleteByIdStreamAsUser()
	{
		SecurityContextHolder.getContext().setAuthentication(user1Authentication);
		testDeleteByIdStream();
	}

	@Test
	public void deleteByIdStreamAsSu()
	{
		SecurityContextHolder.getContext().setAuthentication(adminAuthentication);
		testDeleteByIdStream();
	}

	@Test
	public void deleteByIdStreamAsSystem()
	{
		SecurityContextHolder.getContext().setAuthentication(systemAuthentication);
		testDeleteByIdStream();
	}

	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void deleteAllNoPermission()
	{
		SecurityContextHolder.getContext().setAuthentication(user1Authentication);
		doAnswer(streamConsumer).when(decoratedRepository).deleteAll();
		try
		{
			repositoryDecorator.deleteAll();
		}
		catch (MolgenisDataAccessException e)
		{
			verify(decoratedRepository, times(1)).getEntityMetaData();
			verify(decoratedRepository, never()).deleteAll();
			throw e;
		}
	}

	@Test
	public void deleteAllAsSu()
	{
		SecurityContextHolder.getContext().setAuthentication(adminAuthentication);
		repositoryDecorator.deleteAll();
		verify(decoratedRepository, times(1)).deleteAll();
	}

	@Test
	public void deleteAllAsSystem()
	{
		SecurityContextHolder.getContext().setAuthentication(systemAuthentication);
		repositoryDecorator.deleteAll();
		verify(decoratedRepository, times(1)).deleteAll();
	}

	private void testUpdateStream()
	{
		doAnswer(streamConsumer).when(decoratedRepository).update(any(Stream.class));
		repositoryDecorator.update(Stream.of(entity1, entity2));
		verify(decoratedRepository, times(1)).getEntityMetaData();
		verify(decoratedRepository, times(1)).update(any(Stream.class));
	}

	private void testDeleteStream()
	{
		doAnswer(streamConsumer).when(decoratedRepository).delete(any(Stream.class));
		repositoryDecorator.delete(Stream.of(entity1, entity2));
		verify(decoratedRepository, times(1)).getEntityMetaData();
		verify(decoratedRepository, times(1)).delete(any(Stream.class));
	}

	private void testDeleteByIdStream()
	{
		doAnswer(streamConsumer).when(decoratedRepository).deleteById(any(Stream.class));
		repositoryDecorator.deleteById(Stream.of("entity1", "entity2"));
		verify(decoratedRepository, times(1)).getEntityMetaData();
		verify(decoratedRepository, times(1)).deleteById(any(Stream.class));
	}

	private void testFindOneIdFetch(String permission)
	{
		Object id = mock(Object.class);
		Fetch fetch = mock(Fetch.class);
		when(decoratedRepository.findOne(id, fetch)).thenReturn(entity1);
		repositoryDecorator.findOne(id, fetch);
		verify(decoratedRepository, times(1)).findOne(id, fetch);
		verifyAddPermissionsAttribute(entity1, permission);
	}

	private void testFindOneId(String permission)
	{
		Object id = mock(Object.class);
		when(decoratedRepository.findOne(id)).thenReturn(entity1);
		repositoryDecorator.findOne(id);
		verify(decoratedRepository, times(1)).findOne(id);
		verifyAddPermissionsAttribute(entity1, permission);
	}

	private void testFindOneQuery(String permission)
	{
		Query q = mock(QueryImpl.class);
		when(decoratedRepository.findOne(q)).thenReturn(entity3);
		repositoryDecorator.findOne(q);
		verify(decoratedRepository, times(1)).findOne(q);
		verifyAddPermissionsAttribute(entity3, permission);
	}

	private void testFindAllQuery(String permission1, String permission2)
	{
		Query q = mock(QueryImpl.class);
		when(decoratedRepository.findAll(q)).thenReturn(Stream.of(entity1, entity2));
		repositoryDecorator.findAll(q).collect(Collectors.toList());
		verify(decoratedRepository, times(1)).findAll(q);
		verifyAddPermissionsAttribute(entity1, permission1);
		verifyAddPermissionsAttribute(entity2, permission2);
	}

	private void testFindAllStream(String permission1, String permission2)
	{
		Stream<Object> ids = (Stream<Object>) mock(Stream.class);
		when(decoratedRepository.findAll(ids)).thenReturn(Stream.of(entity1, entity2));
		repositoryDecorator.findAll(ids).collect(Collectors.toList());
		verify(decoratedRepository, times(1)).findAll(ids);
		verifyAddPermissionsAttribute(entity1, permission1);
		verifyAddPermissionsAttribute(entity2, permission2);
	}

	private void testFindAllStreamFetch(String permission1, String permission2)
	{
		Stream<Object> ids = (Stream<Object>) mock(Stream.class);
		Fetch fetch = mock(Fetch.class);
		when(decoratedRepository.findAll(ids, fetch)).thenReturn(Stream.of(entity1, entity2));
		repositoryDecorator.findAll(ids, fetch).collect(Collectors.toList());
		verify(decoratedRepository, times(1)).findAll(ids, fetch);
		verifyAddPermissionsAttribute(entity1, permission1);
		verifyAddPermissionsAttribute(entity2, permission2);
	}

	private void testIterator(String permission1, String permission2, String permission3)
	{
		when(decoratedRepository.iterator()).thenReturn(newArrayList(entity1, entity2, entity3).iterator());
		Iterator<Entity> entityIterator = repositoryDecorator.iterator();
		entityIterator.forEachRemaining(e -> {
		});
		verify(decoratedRepository, times(1)).iterator();
		verifyAddPermissionsAttribute(entity1, permission1);
		verifyAddPermissionsAttribute(entity2, permission2);
		verifyAddPermissionsAttribute(entity3, permission3);
	}

	private void testStream(String permission1, String permission2, String permission3)
	{
		when(decoratedRepository.stream(any(Fetch.class))).thenReturn(Stream.of(entity1, entity2, entity3));
		repositoryDecorator.stream(new Fetch()).collect(Collectors.toList());
		verify(decoratedRepository, times(1)).stream(any(Fetch.class));
		verifyAddPermissionsAttribute(entity1, permission1);
		verifyAddPermissionsAttribute(entity2, permission2);
		verifyAddPermissionsAttribute(entity3, permission3);
	}

	private void verifyAddPermissionsAttribute(Entity entity, String permissions)
	{
		verify(entity, times(1)).set(attributeArgumentCaptor.capture(), permissionsArgumentCaptor.capture());
		assertEquals(attributeArgumentCaptor.getValue(), PERMISSIONS_ATTRIBUTE);
		assertEquals(permissionsArgumentCaptor.getValue(), permissions);
	}

	private void verifyAddUpdateAttribute(Entity entity, List<Entity> users)
	{
		verify(entity, times(1)).set(attributeArgumentCaptor.capture(), usersArgumentCaptor.capture());
		assertEquals(attributeArgumentCaptor.getValue(), UPDATE_ATTRIBUTE);
		assertEquals(usersArgumentCaptor.getValue(), users);
	}
}
