package org.molgenis.data.meta;

import org.molgenis.data.DataService;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.system.SystemEntityMetaDataRegistry;
import org.molgenis.security.core.MolgenisPermissionService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Set;

import static java.util.Collections.singleton;
import static org.mockito.Mockito.*;
import static org.molgenis.data.QueryRule.Operator.EQUALS;
import static org.molgenis.data.RepositoryCapability.WRITABLE;
import static org.testng.Assert.assertEquals;

/**
 * Created by Dennis on 8/1/2016.
 */
public class EntityMetaDataRepositoryDecoratorTest
{
	private EntityMetaDataRepositoryDecorator repo;

	private Repository decoratedRepo;
	private DataService dataService;
	private SystemEntityMetaDataRegistry systemEntityMetaRegistry;
	private MolgenisPermissionService permissionService;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepo = mock(Repository.class);
		dataService = mock(DataService.class);
		systemEntityMetaRegistry = mock(SystemEntityMetaDataRegistry.class);
		permissionService = mock(MolgenisPermissionService.class);
		repo = new EntityMetaDataRepositoryDecorator(decoratedRepo, dataService, systemEntityMetaRegistry,
				permissionService);
	}

	@Test
	public void testGetCapabilities() throws Exception
	{
		Set<RepositoryCapability> decoratedRepoCapabilities = singleton(WRITABLE);
		when(decoratedRepo.getCapabilities()).thenReturn(decoratedRepoCapabilities);
		assertEquals(repo.getCapabilities(), decoratedRepoCapabilities);
	}

	@Test
	public void testClose() throws Exception
	{
		repo.close();
		verify(decoratedRepo, times(1)).close();
	}

	@Test
	public void testGetName() throws Exception
	{
		String name = "repoName";
		when(decoratedRepo.getName()).thenReturn(name);
		assertEquals(repo.getName(), name);
	}

	@Test
	public void testGetQueryOperators() throws Exception
	{
		Set<Operator> decoratedqueryOperators = singleton(EQUALS);
		when(decoratedRepo.getQueryOperators()).thenReturn(decoratedqueryOperators);
		assertEquals(repo.getQueryOperators(), decoratedqueryOperators);
	}

	@Test
	public void testGetEntityMetaData() throws Exception
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(decoratedRepo.getEntityMetaData()).thenReturn(entityMeta);
		assertEquals(repo.getEntityMetaData(), entityMeta);
	}

	@Test
	public void testCount() throws Exception
	{

		repo.count();
	}

	@Test
	public void testQuery() throws Exception
	{

	}

	@Test
	public void testCount1() throws Exception
	{

	}

	@Test
	public void testFindAll() throws Exception
	{

	}

	@Test
	public void testIterator() throws Exception
	{

	}

	@Test
	public void testForEachBatched() throws Exception
	{

	}

	@Test
	public void testFindOne() throws Exception
	{

	}

	@Test
	public void testFindOneById() throws Exception
	{

	}

	@Test
	public void testFindOneById1() throws Exception
	{

	}

	@Test
	public void testFindAll1() throws Exception
	{

	}

	@Test
	public void testFindAll2() throws Exception
	{

	}

	@Test
	public void testAggregate() throws Exception
	{

	}

	@Test
	public void testUpdate() throws Exception
	{

	}

	@Test
	public void testUpdate1() throws Exception
	{

	}

	@Test
	public void testDelete() throws Exception
	{

	}

	@Test
	public void testDelete1() throws Exception
	{

	}

	@Test
	public void testDeleteById() throws Exception
	{

	}

	@Test
	public void testDeleteAll() throws Exception
	{

	}

	@Test
	public void testDeleteAll1() throws Exception
	{

	}

	@Test
	public void testAdd() throws Exception
	{

	}

	@Test
	public void testAdd1() throws Exception
	{

	}

	@Test
	public void testFlush() throws Exception
	{

	}

	@Test
	public void testClearCache() throws Exception
	{

	}

	@Test
	public void testRebuildIndex() throws Exception
	{

	}

}