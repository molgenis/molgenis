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
	private final List<String> entityNames = Arrays.asList("Entity1", "Entity2");
	private Repository repo1;
	private Repository repo2;
	private DataServiceImpl dataService;

	@BeforeMethod
	public void beforeMethod()
	{
		dataService = new DataServiceImpl();

		repo1 = mock(Repository.class);
		when(repo1.getName()).thenReturn("Entity1");
		dataService.addRepository(repo1);

		repo2 = mock(Repository.class);
		when(repo2.getName()).thenReturn("Entity2");
		dataService.addRepository(repo2);
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
		assertEquals(dataService.getRepositoryByEntityName("Entity1"), repo1);
		assertEquals(dataService.getRepositoryByEntityName("Entity2"), repo2);
	}

}
