package org.molgenis.data;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoSession;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.PermissionService;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.mockito.quality.Strictness.STRICT_STUBS;
import static org.molgenis.security.core.Permission.*;
import static org.testng.Assert.*;

public class RepositorySecurityDecoratorTest
{
	private final static String ENTITY_TYPE_ID = "entity";
	private final static String ENTITY_TYPE_LABEL = "Entity under test";
	@Mock
	private Repository<Entity> delegateRepository;
	@Mock
	private PermissionService permissionService;
	@Mock
	private EntityType entityType;
	@Mock
	private Iterator<Entity> entityIterator;
	@Mock
	private Stream<Entity> entities;
	@Mock
	private Stream<Object> ids;
	@Mock
	private Object id;
	@Mock
	private Entity entity;
	@Mock
	private Consumer<List<Entity>> consumer;
	@Mock
	private Fetch fetch;
	@Mock
	private Query<Entity> query;
	@Mock
	private AggregateQuery aggregateQuery;
	@Mock
	private AggregateResult aggregateResult;
	@InjectMocks
	private RepositorySecurityDecorator repositorySecurityDecorator;

	private MockitoSession mockitoSession;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void beforeMethod()
	{
		repositorySecurityDecorator = null;
		mockitoSession = mockitoSession().strictness(STRICT_STUBS).initMocks(this).startMocking();
	}

	@AfterMethod
	public void afterMethod()
	{
		mockitoSession.finishMocking();
	}

	@Test
	public void iterator()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(permissionService.hasPermissionOnEntityType(ENTITY_TYPE_ID, READ)).thenReturn(true);
		when(delegateRepository.iterator()).thenReturn(entityIterator);

		assertSame(repositorySecurityDecorator.iterator(), entityIterator);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[READ\\] permission on entity type \\[Entity under test\\] with id \\[entity\\]")
	public void iteratorNoPermission()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(entityType.getLabel()).thenReturn(ENTITY_TYPE_LABEL);

		try
		{
			repositorySecurityDecorator.iterator();
			fail("Should've thrown exception");
		}
		catch (MolgenisDataAccessException e)
		{
			verify(delegateRepository, times(1)).getEntityType();
			verifyNoMoreInteractions(delegateRepository);
			throw e;
		}
	}

	@Test
	public void count()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(permissionService.hasPermissionOnEntityType(ENTITY_TYPE_ID, COUNT)).thenReturn(true);
		long count = 123;
		when(delegateRepository.count()).thenReturn(count);

		assertSame(repositorySecurityDecorator.count(), count);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[COUNT\\] permission on entity type \\[Entity under test\\] with id \\[entity\\]")
	public void countNoPermission()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(entityType.getLabel()).thenReturn(ENTITY_TYPE_LABEL);

		try
		{
			repositorySecurityDecorator.count();
			fail("Should've thrown exception");
		}
		catch (MolgenisDataAccessException e)
		{
			verify(delegateRepository, times(1)).getEntityType();
			verifyNoMoreInteractions(delegateRepository);
			throw e;
		}
	}

	@Test
	public void countQuery()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(permissionService.hasPermissionOnEntityType(ENTITY_TYPE_ID, COUNT)).thenReturn(true);
		long count = 123;
		when(delegateRepository.count(query)).thenReturn(count);

		assertSame(repositorySecurityDecorator.count(query), count);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[COUNT\\] permission on entity type \\[Entity under test\\] with id \\[entity\\]")
	public void countQueryNoPermission()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(entityType.getLabel()).thenReturn(ENTITY_TYPE_LABEL);

		try
		{
			repositorySecurityDecorator.count(query);
			fail("Should've thrown exception");
		}
		catch (MolgenisDataAccessException e)
		{
			verify(delegateRepository, times(1)).getEntityType();
			verifyNoMoreInteractions(delegateRepository);
			throw e;
		}
	}

	@Test
	public void findOneQuery()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(permissionService.hasPermissionOnEntityType(ENTITY_TYPE_ID, READ)).thenReturn(true);
		when(delegateRepository.findOne(query)).thenReturn(entity);

		assertSame(repositorySecurityDecorator.findOne(query), entity);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[READ\\] permission on entity type \\[Entity under test\\] with id \\[entity\\]")
	public void findOneQueryNoPermission()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(entityType.getLabel()).thenReturn(ENTITY_TYPE_LABEL);

		try
		{
			repositorySecurityDecorator.findOne(query);
			fail("Should've thrown exception");
		}
		catch (MolgenisDataAccessException e)
		{
			verify(delegateRepository, times(1)).getEntityType();
			verifyNoMoreInteractions(delegateRepository);
			throw e;
		}
	}

	@Test
	public void findOneById()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(permissionService.hasPermissionOnEntityType(ENTITY_TYPE_ID, READ)).thenReturn(true);
		when(delegateRepository.findOneById(id)).thenReturn(entity);

		assertSame(repositorySecurityDecorator.findOneById(id), entity);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[READ\\] permission on entity type \\[Entity under test\\] with id \\[entity\\]")
	public void findOneByIdNoPermission()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(entityType.getLabel()).thenReturn(ENTITY_TYPE_LABEL);

		try
		{
			repositorySecurityDecorator.findOneById(id);
			fail("Should've thrown exception");
		}
		catch (MolgenisDataAccessException e)
		{
			verify(delegateRepository, times(1)).getEntityType();
			verifyNoMoreInteractions(delegateRepository);
			throw e;
		}
	}

	@Test
	public void findOneByIdAndFetch()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(permissionService.hasPermissionOnEntityType(ENTITY_TYPE_ID, READ)).thenReturn(true);
		when(delegateRepository.findOneById(id, fetch)).thenReturn(entity);

		assertSame(repositorySecurityDecorator.findOneById(id, fetch), entity);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[READ\\] permission on entity type \\[Entity under test\\] with id \\[entity\\]")
	public void findOneByIdAndFetchNoPermission()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(entityType.getLabel()).thenReturn(ENTITY_TYPE_LABEL);

		try
		{
			repositorySecurityDecorator.findOneById(id, fetch);
			fail("Should've thrown exception");
		}
		catch (MolgenisDataAccessException e)
		{
			verify(delegateRepository, times(1)).getEntityType();
			verifyNoMoreInteractions(delegateRepository);
			throw e;
		}
	}

	@Test
	public void addEntity()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(permissionService.hasPermissionOnEntityType(ENTITY_TYPE_ID, WRITE)).thenReturn(true);

		repositorySecurityDecorator.add(entity);

		verify(delegateRepository).add(entity);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[WRITE\\] permission on entity type \\[Entity under test\\] with id \\[entity\\]")
	public void addEntityNoPermission()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(entityType.getLabel()).thenReturn(ENTITY_TYPE_LABEL);

		try
		{
			repositorySecurityDecorator.add(entity);
			fail("Should've thrown exception");
		}
		catch (MolgenisDataAccessException e)
		{
			verify(delegateRepository, times(1)).getEntityType();
			verifyNoMoreInteractions(delegateRepository);
			throw e;
		}
	}

	@Test
	public void addStream()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(permissionService.hasPermissionOnEntityType(ENTITY_TYPE_ID, WRITE)).thenReturn(true);
		Integer count = 123;
		when(delegateRepository.add(entities)).thenReturn(count);

		assertSame(repositorySecurityDecorator.add(entities), count);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[WRITE\\] permission on entity type \\[Entity under test\\] with id \\[entity\\]")
	public void addStreamNoPermission()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(entityType.getLabel()).thenReturn(ENTITY_TYPE_LABEL);

		try
		{
			repositorySecurityDecorator.add(entities);
			fail("Should've thrown exception");
		}
		catch (MolgenisDataAccessException e)
		{
			verify(delegateRepository, times(1)).getEntityType();
			verifyNoMoreInteractions(delegateRepository);
			throw e;
		}
	}

	@Test
	public void deleteStream()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(permissionService.hasPermissionOnEntityType(ENTITY_TYPE_ID, WRITE)).thenReturn(true);

		repositorySecurityDecorator.delete(entities);

		verify(delegateRepository, times(1)).delete(entities);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[WRITE\\] permission on entity type \\[Entity under test\\] with id \\[entity\\]")
	public void deleteStreamNoPermission()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(entityType.getLabel()).thenReturn(ENTITY_TYPE_LABEL);
		try
		{
			repositorySecurityDecorator.delete(entities);
			fail("Should've thrown security exception");
		}
		catch (MolgenisDataAccessException e)
		{
			verify(delegateRepository, times(1)).getEntityType();
			verifyNoMoreInteractions(delegateRepository);
			throw e;
		}
	}

	@Test
	public void delete()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(permissionService.hasPermissionOnEntityType(ENTITY_TYPE_ID, WRITE)).thenReturn(true);

		repositorySecurityDecorator.delete(entity);

		verify(delegateRepository).delete(entity);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[WRITE\\] permission on entity type \\[Entity under test\\] with id \\[entity\\]")
	public void deleteNoPermission()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(entityType.getLabel()).thenReturn(ENTITY_TYPE_LABEL);

		try
		{
			repositorySecurityDecorator.delete(entity);
			fail("Should've thrown security exception");
		}
		catch (MolgenisDataAccessException e)
		{
			verify(delegateRepository, times(1)).getEntityType();
			verifyNoMoreInteractions(delegateRepository);
			throw e;
		}
	}

	@Test
	public void deleteById()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(permissionService.hasPermissionOnEntityType(ENTITY_TYPE_ID, WRITE)).thenReturn(true);

		repositorySecurityDecorator.deleteById(id);

		verify(delegateRepository).deleteById(id);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[WRITE\\] permission on entity type \\[Entity under test\\] with id \\[entity\\]")
	public void deleteByIdNoPermission()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(entityType.getLabel()).thenReturn(ENTITY_TYPE_LABEL);

		try
		{
			repositorySecurityDecorator.deleteById(id);
			fail("Should've thrown security exception");
		}
		catch (MolgenisDataAccessException e)
		{
			verify(delegateRepository, times(1)).getEntityType();
			verifyNoMoreInteractions(delegateRepository);
			throw e;
		}
	}

	@Test
	public void deleteAllIdStream()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(permissionService.hasPermissionOnEntityType(ENTITY_TYPE_ID, WRITE)).thenReturn(true);

		repositorySecurityDecorator.deleteAll(ids);

		verify(delegateRepository).deleteAll(ids);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[WRITE\\] permission on entity type \\[Entity under test\\] with id \\[entity\\]")
	public void deleteAllIdStreamNoPermission()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(entityType.getLabel()).thenReturn(ENTITY_TYPE_LABEL);

		try
		{
			repositorySecurityDecorator.deleteAll(ids);
			fail("Should've thrown security exception");
		}
		catch (MolgenisDataAccessException e)
		{
			verify(delegateRepository, times(1)).getEntityType();
			verifyNoMoreInteractions(delegateRepository);
			throw e;
		}
	}

	@Test
	public void deleteAll()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(permissionService.hasPermissionOnEntityType(ENTITY_TYPE_ID, WRITE)).thenReturn(true);

		repositorySecurityDecorator.deleteAll();

		verify(delegateRepository).deleteAll();
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[WRITE\\] permission on entity type \\[Entity under test\\] with id \\[entity\\]")
	public void deleteAllNoPermission()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(entityType.getLabel()).thenReturn(ENTITY_TYPE_LABEL);

		try
		{
			repositorySecurityDecorator.deleteAll();
			fail("Should've thrown security exception");
		}
		catch (MolgenisDataAccessException e)
		{
			verify(delegateRepository, times(1)).getEntityType();
			verifyNoMoreInteractions(delegateRepository);
			throw e;
		}
	}

	@Test
	public void update()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(permissionService.hasPermissionOnEntityType(ENTITY_TYPE_ID, WRITE)).thenReturn(true);

		repositorySecurityDecorator.update(entity);

		verify(delegateRepository).update(entity);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[WRITE\\] permission on entity type \\[Entity under test\\] with id \\[entity\\]")
	public void updateNoPermission()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(entityType.getLabel()).thenReturn(ENTITY_TYPE_LABEL);

		try
		{
			repositorySecurityDecorator.update(entity);
			fail("Should've thrown security exception");
		}
		catch (MolgenisDataAccessException e)
		{
			verify(delegateRepository, times(1)).getEntityType();
			verifyNoMoreInteractions(delegateRepository);
			throw e;
		}
	}

	@Test
	public void updateStream()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(permissionService.hasPermissionOnEntityType(ENTITY_TYPE_ID, WRITE)).thenReturn(true);

		repositorySecurityDecorator.update(entities);

		verify(delegateRepository).update(entities);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[WRITE\\] permission on entity type \\[Entity under test\\] with id \\[entity\\]")
	public void updateStreamNoPermission()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(entityType.getLabel()).thenReturn(ENTITY_TYPE_LABEL);

		try
		{
			repositorySecurityDecorator.update(entities);
			fail("Should've thrown security exception");
		}
		catch (MolgenisDataAccessException e)
		{
			verify(delegateRepository, times(1)).getEntityType();
			verifyNoMoreInteractions(delegateRepository);
			throw e;
		}
	}

	@Test
	public void findAllStream()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(permissionService.hasPermissionOnEntityType(ENTITY_TYPE_ID, READ)).thenReturn(true);
		when(delegateRepository.findAll(ids)).thenReturn(entities);

		assertSame(repositorySecurityDecorator.findAll(ids), entities);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[READ\\] permission on entity type \\[Entity under test\\] with id \\[entity\\]")
	public void findAllStreamNoPermission()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(entityType.getLabel()).thenReturn(ENTITY_TYPE_LABEL);

		repositorySecurityDecorator.findAll(ids);
	}

	@Test
	public void findAllStreamFetch()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(permissionService.hasPermissionOnEntityType(ENTITY_TYPE_ID, READ)).thenReturn(true);
		when(delegateRepository.findAll(ids, fetch)).thenReturn(entities);

		assertSame(repositorySecurityDecorator.findAll(ids, fetch), entities);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[READ\\] permission on entity type \\[Entity under test\\] with id \\[entity\\]")
	public void findAllStreamFetchNoPermission()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(entityType.getLabel()).thenReturn(ENTITY_TYPE_LABEL);
		try
		{
			repositorySecurityDecorator.findAll(ids, fetch);
			fail("Should've thrown security exception");
		}
		catch (MolgenisDataException expected)
		{
			verify(delegateRepository).getEntityType();
			verifyNoMoreInteractions(delegateRepository);
			throw expected;
		}
	}

	@Test
	public void findOnePermission()
	{
		when(delegateRepository.findOneById(id, fetch)).thenReturn(entity);
		assertEquals(delegateRepository.findOneById(id, fetch), entity);
		verify(delegateRepository, times(1)).findOneById(id, fetch);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[READ\\] permission on entity type \\[Entity under test\\] with id \\[entity\\]")
	public void findOneNoPermission()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(entityType.getLabel()).thenReturn(ENTITY_TYPE_LABEL);
		try
		{
			repositorySecurityDecorator.findOneById(id, fetch);
			fail("Should've thrown security exception");
		}
		catch (MolgenisDataException expected)
		{
			verify(delegateRepository).getEntityType();
			verifyNoMoreInteractions(delegateRepository);
			throw expected;
		}
	}

	@Test
	public void findAllQueryPermission()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(permissionService.hasPermissionOnEntityType(ENTITY_TYPE_ID, READ)).thenReturn(true);
		when(delegateRepository.findAll(query)).thenReturn(entities);

		assertSame(repositorySecurityDecorator.findAll(query), entities);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[READ\\] permission on entity type \\[Entity under test\\] with id \\[entity\\]")
	public void findAllQueryNoPermission()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(entityType.getLabel()).thenReturn(ENTITY_TYPE_LABEL);

		try
		{
			repositorySecurityDecorator.findAll(query);
			fail("Should've thrown security exception");
		}
		catch (MolgenisDataException expected)
		{
			verify(delegateRepository, times(1)).getEntityType();
			verifyNoMoreInteractions(delegateRepository);
			throw expected;
		}
	}

	@Test
	public void streamFetch()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(permissionService.hasPermissionOnEntityType(ENTITY_TYPE_ID, READ)).thenReturn(true);

		repositorySecurityDecorator.forEachBatched(fetch, consumer, 1000);

		verify(delegateRepository).forEachBatched(fetch, consumer, 1000);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[READ\\] permission on entity type \\[Entity under test\\] with id \\[entity\\]")
	public void streamFetchNoPermission()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(entityType.getLabel()).thenReturn(ENTITY_TYPE_LABEL);

		try
		{
			repositorySecurityDecorator.forEachBatched(fetch, consumer, 1000);
			fail("Should've thrown security exception");
		}
		catch (MolgenisDataException expected)
		{
			verify(delegateRepository, times(1)).getEntityType();
			verifyNoMoreInteractions(delegateRepository);
			throw expected;
		}
	}

	@Test
	public void aggregate()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(permissionService.hasPermissionOnEntityType(ENTITY_TYPE_ID, COUNT)).thenReturn(true);
		when(repositorySecurityDecorator.aggregate(aggregateQuery)).thenReturn(aggregateResult);

		assertEquals(repositorySecurityDecorator.aggregate(aggregateQuery), aggregateResult);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No \\[COUNT\\] permission on entity type \\[Entity under test\\] with id \\[entity\\]")
	public void aggregateNoPermission()
	{
		when(delegateRepository.getEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(ENTITY_TYPE_ID);
		when(permissionService.hasPermissionOnEntityType(ENTITY_TYPE_ID, COUNT)).thenReturn(false);
		when(entityType.getLabel()).thenReturn(ENTITY_TYPE_LABEL);

		repositorySecurityDecorator.aggregate(aggregateQuery);
	}
}