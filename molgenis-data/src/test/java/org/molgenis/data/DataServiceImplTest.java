package org.molgenis.data;

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

import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.NonDecoratingRepositoryDecoratorFactory;
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
	public void findAllStringIterableFetch()
	{
		Iterable<Object> ids = Arrays.asList(0);
		Fetch fetch = new Fetch();
		Iterable<Entity> entities = Arrays.asList(mock(Entity.class));
		when(repo1.findAll(ids, fetch)).thenReturn(entities);
		assertEquals(dataService.findAll("Entity1", ids, fetch), entities);
		verify(repo1, times(1)).findAll(ids, fetch);
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
	public void findAllStringIterableFetchClass()
	{
		Iterable<Object> ids = Arrays.asList(0);
		Fetch fetch = new Fetch();
		Class<Entity> clazz = Entity.class;
		Iterable<Entity> entities = Arrays.asList(mock(Entity.class));
		when(repo1.findAll(ids, fetch)).thenReturn(entities);
		// how to check return value? converting iterable can't be mocked.
		dataService.findAll("Entity1", ids, fetch, clazz);
		verify(repo1, times(1)).findAll(ids, fetch);
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
}
