package org.molgenis.security.owned;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.support.OwnedEntityMetaData.ATTR_OWNER_USERNAME;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.molgenis.data.support.OwnedEntityMetaData;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class OwnedEntityRepositoryDecoratorTest
{
	private EntityMetaData entityMeta;
	private Repository decoratedRepository;
	private OwnedEntityRepositoryDecorator ownedEntityRepositoryDecorator;

	@BeforeMethod
	public void setUp()
	{
		entityMeta = mock(EntityMetaData.class);
		decoratedRepository = mock(Repository.class);
		when(decoratedRepository.getEntityMetaData()).thenReturn(entityMeta);
		ownedEntityRepositoryDecorator = new OwnedEntityRepositoryDecorator(decoratedRepository);
	}

	@Test
	public void findAllIterableFetchExtendsOwned()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(entityMeta.getExtends()).thenReturn(new OwnedEntityMetaData());

		Iterable<Object> ids = Arrays.<Object> asList(Integer.valueOf(0), Integer.valueOf(1));
		Fetch fetch = new Fetch();
		Entity myEntity = when(mock(Entity.class).getString(ATTR_OWNER_USERNAME)).thenReturn("username").getMock();
		Entity notMyEntity = when(mock(Entity.class).getString(ATTR_OWNER_USERNAME)).thenReturn("notme").getMock();
		Iterable<Entity> entities = Arrays.asList(myEntity, notMyEntity);
		when(decoratedRepository.findAll(ids, fetch)).thenReturn(entities);
		assertEquals(Arrays.asList(myEntity), Lists.newArrayList(ownedEntityRepositoryDecorator.findAll(ids, fetch)));
		verify(decoratedRepository, times(1)).findAll(ids, fetch);
	}

	@Test
	public void findAllIterableFetchNotExtendsOwned()
	{
		Iterable<Object> ids = Arrays.<Object> asList(Integer.valueOf(0), Integer.valueOf(1));
		Fetch fetch = new Fetch();
		Iterable<Entity> entities = Arrays.asList(mock(Entity.class), mock(Entity.class));
		when(decoratedRepository.findAll(ids, fetch)).thenReturn(entities);
		assertEquals(entities, ownedEntityRepositoryDecorator.findAll(ids, fetch));
		verify(decoratedRepository, times(1)).findAll(ids, fetch);
	}

	@Test
	public void findOneObjectFetchExtendsOwned()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(entityMeta.getExtends()).thenReturn(new OwnedEntityMetaData());

		Object id = Integer.valueOf(0);
		Fetch fetch = new Fetch();
		Entity myEntity = when(mock(Entity.class).getString(ATTR_OWNER_USERNAME)).thenReturn("username").getMock();
		when(decoratedRepository.findOne(id, fetch)).thenReturn(myEntity);
		assertEquals(myEntity, ownedEntityRepositoryDecorator.findOne(id, fetch));
		verify(decoratedRepository, times(1)).findOne(id, fetch);
	}

	@Test
	public void findOneObjectFetchExtendsOwnedBySomeoneElse()
	{
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(entityMeta.getExtends()).thenReturn(new OwnedEntityMetaData());

		Object id = Integer.valueOf(0);
		Fetch fetch = new Fetch();
		Entity myEntity = when(mock(Entity.class).getString(ATTR_OWNER_USERNAME)).thenReturn("notme").getMock();
		when(decoratedRepository.findOne(id, fetch)).thenReturn(myEntity);
		assertNull(ownedEntityRepositoryDecorator.findOne(id, fetch));
		verify(decoratedRepository, times(1)).findOne(id, fetch);
	}

	@Test
	public void findOneObjectFetchNotExtendsOwned()
	{
		Object id = Integer.valueOf(0);
		Fetch fetch = new Fetch();
		Entity entity = mock(Entity.class);
		when(decoratedRepository.findOne(id, fetch)).thenReturn(entity);
		ownedEntityRepositoryDecorator.findOne(id, fetch);
		verify(decoratedRepository, times(1)).findOne(id, fetch);
	}
}
