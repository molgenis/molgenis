package org.molgenis.data;

import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mockito.ArgumentCaptor;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.NonDecoratingRepositoryDecoratorFactory;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataServiceImplTest
{
	private final List<String> entityNames = Arrays.asList("Entity1", "Entity2", "Entity3");
	private Repository repo1;
	private Repository repo2;
	private Repository repoToRemove;
	private DataServiceImpl dataService;

	@BeforeMethod
	public void beforeMethod()
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

		dataService = new DataServiceImpl(new NonDecoratingRepositoryDecoratorFactory());

		repo1 = mock(Repository.class);
		when(repo1.getName()).thenReturn("Entity1");
		dataService.addRepository(repo1);

		repo2 = mock(Repository.class);
		when(repo2.getName()).thenReturn("Entity2");
		dataService.addRepository(repo2);

		repoToRemove = mock(Repository.class);
		when(repoToRemove.getName()).thenReturn("Entity3");
		dataService.addRepository(repoToRemove);

	}

	@Test
	public void addStream()
	{
		Stream<Entity> entities = Stream.empty();
		dataService.add("Entity1", entities);
		verify(repo1, times(1)).add(entities);
	}

	@Test
	public void updateStream()
	{
		Stream<Entity> entities = Stream.empty();
		dataService.update("Entity1", entities);
		verify(repo1, times(1)).update(entities);
	}

	@Test
	public void deleteStream()
	{
		Stream<Entity> entities = Stream.empty();
		dataService.delete("Entity1", entities);
		verify(repo1, times(1)).delete(entities);
	}

	@Test
	public void getEntityNames()
	{
		assertNotNull(dataService.getEntityNames());
		Iterator<String> it = dataService.getEntityNames().iterator();
		assertTrue(it.hasNext());
		assertTrue(it.next().equalsIgnoreCase(entityNames.get(0)));
		assertTrue(it.hasNext());
		assertTrue(it.next().equalsIgnoreCase(entityNames.get(1)));
		assertTrue(it.hasNext());
		assertTrue(it.next().equalsIgnoreCase(entityNames.get(2)));
		assertFalse(it.hasNext());
	}

	@Test
	public void getRepositoryByEntityName()
	{
		assertEquals(dataService.getRepository("Entity1"), repo1);
		assertEquals(dataService.getRepository("Entity2"), repo2);
	}

	@Test
	public void removeRepositoryByEntityName()
	{
		assertEquals(dataService.getRepository("Entity3"), repoToRemove);
		dataService.removeRepository("Entity3");
	}

	@Test(expectedExceptions = UnknownEntityException.class)
	public void removeRepositoryByEntityNameUnknownEntityException()
	{
		assertEquals(dataService.getRepository("Entity3"), repoToRemove);
		dataService.removeRepository("Entity3");
		dataService.getRepository("Entity3");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void removeRepositoryByEntityNameMolgenisDataException()
	{
		assertEquals(dataService.getRepository("Entity3"), repoToRemove);
		dataService.removeRepository("Entity4");
	}

	@Test
	public void findOneStringObjectFetch()
	{
		Object id = Integer.valueOf(0);
		Fetch fetch = new Fetch();
		Entity entity = mock(Entity.class);
		when(repo1.findOne(id, fetch)).thenReturn(entity);
		assertEquals(dataService.findOne("Entity1", id, fetch), entity);
		verify(repo1, times(1)).findOne(id, fetch);
	}

	@Test
	public void findOneStringObjectFetchEntityNull()
	{
		Object id = Integer.valueOf(0);
		Fetch fetch = new Fetch();
		when(repo1.findOne(id, fetch)).thenReturn(null);
		assertNull(dataService.findOne("Entity1", id, fetch));
		verify(repo1, times(1)).findOne(id, fetch);
	}

	@Test
	public void findOneStringObjectFetchClass()
	{
		Object id = Integer.valueOf(0);
		Fetch fetch = new Fetch();
		Class<Entity> clazz = Entity.class;
		Entity entity = mock(Entity.class);
		when(repo1.findOne(id, fetch)).thenReturn(entity);
		// how to check return value? converting iterable can't be mocked.
		dataService.findOne("Entity1", id, fetch, clazz);
		verify(repo1, times(1)).findOne(id, fetch);
	}

	@Test
	public void findOneStringObjectFetchClassEntityNull()
	{
		Object id = Integer.valueOf(0);
		Fetch fetch = new Fetch();
		Class<Entity> clazz = Entity.class;
		when(repo1.findOne(id, fetch)).thenReturn(null);
		assertNull(dataService.findOne("Entity1", id, fetch, clazz));
		verify(repo1, times(1)).findOne(id, fetch);
	}

	@Test
	public void findAllStringStream()
	{
		Object id0 = "id0";
		Stream<Object> ids = Stream.of(id0);
		Entity entity0 = mock(Entity.class);
		when(repo1.findAll(ids)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.findAll("Entity1", ids);
		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0));
	}

	@Test
	public void findAllStringStreamClass()
	{
		Object id0 = "id0";
		Stream<Object> ids = Stream.of(id0);
		Entity entity0 = mock(Entity.class);
		Class<Entity> clazz = Entity.class;
		when(repo1.findAll(ids)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.findAll("Entity1", ids, clazz);
		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0));
	}

	@Test
	public void findAllStringStreamFetch()
	{
		Object id0 = "id0";
		Stream<Object> ids = Stream.of(id0);
		Entity entity0 = mock(Entity.class);
		Fetch fetch = new Fetch();
		when(repo1.findAll(ids, fetch)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.findAll("Entity1", ids, fetch);
		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0));
	}

	@Test
	public void findAllStringStreamFetchClass()
	{
		Object id0 = "id0";
		Stream<Object> ids = Stream.of(id0);
		Entity entity0 = mock(Entity.class);
		Class<Entity> clazz = Entity.class;
		Fetch fetch = new Fetch();
		when(repo1.findAll(ids, fetch)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.findAll("Entity1", ids, fetch, clazz);
		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0));
	}

	@Test
	public void findAllStreamString()
	{
		Entity entity0 = mock(Entity.class);
		when(repo1.findAll(new QueryImpl())).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.findAll("Entity1");
		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0));
	}

	@Test
	public void findAllStreamStringClass()
	{
		Class<Entity> clazz = Entity.class;
		Entity entity0 = mock(Entity.class);
		when(repo1.findAll(new QueryImpl())).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.findAll("Entity1", clazz);
		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0));
	}

	@Test
	public void findAllStreamStringQuery()
	{
		Entity entity0 = mock(Entity.class);
		Query query = mock(Query.class);
		when(repo1.findAll(query)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.findAll("Entity1", query);
		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0));
	}

	@Test
	public void findAllStreamStringQueryClass()
	{
		Class<Entity> clazz = Entity.class;
		Entity entity0 = mock(Entity.class);
		Query query = mock(Query.class);
		when(repo1.findAll(query)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.findAll("Entity1", query, clazz);
		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0));
	}

	@Test
	public void copyRepository()
	{
		// setup everything
		Query query = new QueryImpl();
		AttributeMetaData attr1 = new DefaultAttributeMetaData("attr1", MolgenisFieldTypes.FieldTypeEnum.STRING);
		AttributeMetaData attr2 = new DefaultAttributeMetaData("attr2", MolgenisFieldTypes.FieldTypeEnum.STRING);

		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		EntityMetaData emd = mock(EntityMetaData.class);
		MetaDataService metaDataService = mock(MetaDataService.class);

		when(repo1.findAll(query)).thenReturn(Stream.of(entity0, entity1));
		when(repo1.getEntityMetaData()).thenReturn(emd);
		when(emd.getOwnAttributes()).thenReturn(Arrays.asList(attr1, attr2));
		when(emd.getOwnLookupAttributes()).thenReturn(Arrays.asList(attr1, attr2));

		dataService.setMeta(metaDataService);

		EntityMetaData emd2 = new DefaultEntityMetaData("Entity2", emd);
		when(repo2.getEntityMetaData()).thenReturn(emd2);
		when(metaDataService.addEntityMeta(emd2)).thenReturn(repo2);

		// The actual method call
		Repository copy = dataService.copyRepository(repo1, "Entity2", "testCopyLabel");

		// The test
		verify(metaDataService).addEntityMeta(copy.getEntityMetaData());
		@SuppressWarnings(
		{ "unchecked", "rawtypes" })
		ArgumentCaptor<Stream<Entity>> argument = ArgumentCaptor.forClass((Class) Stream.class);
		verify(repo2, times(1)).add(argument.capture());
		List<Entity> list = argument.getAllValues().get(0).collect(toList());
		assertEquals(list, Arrays.asList(entity0, entity1));
	}

	@Test
	public void streamStringFetch()
	{
		Entity entity0 = mock(Entity.class);
		Fetch fetch = new Fetch();
		when(repo1.stream(fetch)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.stream("Entity1", fetch);
		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0));
	}

	@Test
	public void streamStringFetchClass()
	{
		Entity entity0 = mock(Entity.class);
		Class<Entity> clazz = Entity.class;
		Fetch fetch = new Fetch();
		when(repo1.stream(fetch)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.stream("Entity1", fetch, clazz);
		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0));
	}
}
