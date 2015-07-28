package org.molgenis.data;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.molgenis.data.support.DataServiceImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataServiceImplTest
{
	private final String url = "test://";
	private final List<String> entityNames = Arrays.asList("Entity1", "Entity2");
	@SuppressWarnings("rawtypes")
	private Repository repo;
	private EntitySource entitySource;
	private DataServiceImpl dataService;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void beforeMethod()
	{
		repo = mock(Repository.class);

		entitySource = mock(EntitySource.class);
		when(entitySource.getUrl()).thenReturn(url);
		when(entitySource.getEntityNames()).thenReturn(entityNames);
		when(entitySource.getRepositoryByEntityName("Entity1")).thenReturn(repo);
		when(entitySource.getRepositoryByEntityName("Entity2")).thenReturn(repo);

		EntitySourceFactory factory = mock(EntitySourceFactory.class);
		when(factory.getUrlPrefix()).thenReturn("test");
		when(factory.create(url)).thenReturn(entitySource);

		dataService = new DataServiceImpl();
		dataService.registerFactory(factory);
		dataService.registerEntitySource(url);
	}

	@Test
	public void getEntityNames()
	{
		assertNotNull(dataService.getEntityNames());
		Iterator<String> it = dataService.getEntityNames().iterator();
		assertTrue(it.hasNext());
		assertEquals(it.next(), entityNames.get(0));
		assertTrue(it.hasNext());
		assertEquals(it.next(), entityNames.get(1));
		assertFalse(it.hasNext());
	}

	@Test
	public void getRepositoryByEntityName()
	{
		assertEquals(dataService.getRepositoryByEntityName("Entity1"), repo);
		assertEquals(dataService.getRepositoryByEntityName("Entity2"), repo);
	}

	@Test
	public void iterator()
	{
		Iterator<EntitySource> it = dataService.iterator();
		assertNotNull(it);
		assertTrue(it.hasNext());
		assertEquals(it.next(), entitySource);
		assertFalse(it.hasNext());
	}
}
