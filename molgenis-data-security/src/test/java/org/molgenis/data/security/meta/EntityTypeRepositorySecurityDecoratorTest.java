package org.molgenis.data.security.meta;

import org.mockito.Mock;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.PermissionService;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class EntityTypeRepositorySecurityDecoratorTest extends AbstractMockitoTest
{
	@Mock
	private Repository<EntityType> delegateRepository;
	@Mock
	private PermissionService permissionService;

	private EntityTypeRepositorySecurityDecorator entityTypeRepositorySecurityDecorator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entityTypeRepositorySecurityDecorator = new EntityTypeRepositorySecurityDecorator(delegateRepository,
				permissionService);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testEntityTypeRepositorySecurityDecorator()
	{
		new EntityTypeRepositorySecurityDecorator(null, null);
	}

	@Test
	public void testIterator()
	{
		EntityType entityType0 = createEntityTypeWithPermission("id0", Permission.READ);
		EntityType entityType1 = createEntityTypeWithPermission("id1", Permission.WRITE);
		when(delegateRepository.iterator()).thenReturn(asList(entityType0, entityType1).iterator());

		assertEquals(newArrayList(entityTypeRepositorySecurityDecorator.iterator()), asList(entityType0, entityType1));
		verify(entityType0).setReadOnly(true);
		verify(entityType1, never()).setReadOnly(anyBoolean());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testForEachBatched()
	{
		EntityType entityType0 = createEntityTypeWithPermission("id0", Permission.READ);
		EntityType entityType1 = createEntityTypeWithPermission("id1", Permission.WRITE);
		Fetch fetch = mock(Fetch.class);

		int batchSize = 100;
		doAnswer(invocation ->
		{
			Consumer<List<EntityType>> delegateConsumer = invocation.getArgument(1);
			delegateConsumer.accept(asList(entityType0, entityType1));
			return null;
		}).when(delegateRepository).forEachBatched(eq(fetch), any(Consumer.class), eq(batchSize));

		Consumer<List<EntityType>> consumer = mock(Consumer.class);
		entityTypeRepositorySecurityDecorator.forEachBatched(fetch, consumer, batchSize);
		verify(consumer).accept(asList(entityType0, entityType1));
		verify(entityType0).setReadOnly(true);
		verify(entityType1, never()).setReadOnly(anyBoolean());
	}

	@Test
	public void testFindAllQuery()
	{
		EntityType entityType0 = createEntityTypeWithPermission("id0", Permission.READ);
		EntityType entityType1 = createEntityTypeWithPermission("id1", Permission.WRITE);
		@SuppressWarnings("unchecked")
		Query<EntityType> query = mock(Query.class);
		when(delegateRepository.findAll(query)).thenReturn(Stream.of(entityType0, entityType1));

		assertEquals(entityTypeRepositorySecurityDecorator.findAll(query).collect(toList()),
				asList(entityType0, entityType1));
		verify(entityType0).setReadOnly(true);
		verify(entityType1, never()).setReadOnly(anyBoolean());
	}

	@Test
	public void testFindOneQueryPermissionRead()
	{
		EntityType entityType = createEntityTypeWithPermission("id", Permission.READ);
		@SuppressWarnings("unchecked")
		Query<EntityType> query = mock(Query.class);
		when(delegateRepository.findOne(query)).thenReturn(entityType);

		assertEquals(entityTypeRepositorySecurityDecorator.findOne(query), entityType);
		verify(entityType).setReadOnly(true);
	}

	@Test
	public void testFindOneQueryPermissionWrite()
	{
		EntityType entityType = createEntityTypeWithPermission("id", Permission.WRITE);
		@SuppressWarnings("unchecked")
		Query<EntityType> query = mock(Query.class);
		when(delegateRepository.findOne(query)).thenReturn(entityType);

		assertEquals(entityTypeRepositorySecurityDecorator.findOne(query), entityType);
		verify(entityType, never()).setReadOnly(anyBoolean());
	}

	@Test
	public void testFindOneByIdObjectPermissionRead()
	{
		String entityTypeId = "id";
		EntityType entityType = createEntityTypeWithPermission(entityTypeId, Permission.READ);
		when(delegateRepository.findOneById(entityTypeId)).thenReturn(entityType);

		assertEquals(entityTypeRepositorySecurityDecorator.findOneById(entityTypeId), entityType);
		verify(entityType).setReadOnly(true);
	}

	@Test
	public void testFindOneByIdObjectPermissionWrite()
	{
		String entityTypeId = "id";
		EntityType entityType = createEntityTypeWithPermission(entityTypeId, Permission.WRITE);
		when(delegateRepository.findOneById(entityTypeId)).thenReturn(entityType);

		assertEquals(entityTypeRepositorySecurityDecorator.findOneById(entityTypeId), entityType);
		verify(entityType, never()).setReadOnly(anyBoolean());
	}

	@Test
	public void testFindOneByIdObjectFetchPermissionRead()
	{
		String entityTypeId = "id";
		EntityType entityType = createEntityTypeWithPermission(entityTypeId, Permission.READ);
		Fetch fetch = mock(Fetch.class);
		when(delegateRepository.findOneById(entityTypeId, fetch)).thenReturn(entityType);

		assertEquals(entityTypeRepositorySecurityDecorator.findOneById(entityTypeId, fetch), entityType);
		verify(entityType).setReadOnly(true);
	}

	@Test
	public void testFindOneByIdObjectFetchPermissionWrite()
	{
		String entityTypeId = "id";
		EntityType entityType = createEntityTypeWithPermission(entityTypeId, Permission.WRITE);
		Fetch fetch = mock(Fetch.class);
		when(delegateRepository.findOneById(entityTypeId, fetch)).thenReturn(entityType);

		assertEquals(entityTypeRepositorySecurityDecorator.findOneById(entityTypeId, fetch), entityType);
		verify(entityType, never()).setReadOnly(anyBoolean());
	}

	@Test
	public void testFindAllStream()
	{
		String entityTypeId0 = "id0";
		EntityType entityType0 = createEntityTypeWithPermission(entityTypeId0, Permission.READ);
		String entityTypeId1 = "id1";
		EntityType entityType1 = createEntityTypeWithPermission(entityTypeId1, Permission.WRITE);
		Stream<Object> entityTypeIdStream = Stream.of(entityType0, entityType1);
		when(delegateRepository.findAll(entityTypeIdStream)).thenReturn(Stream.of(entityType0, entityType1));

		assertEquals(entityTypeRepositorySecurityDecorator.findAll(entityTypeIdStream).collect(toList()),
				asList(entityType0, entityType1));
		verify(entityType0).setReadOnly(true);
		verify(entityType1, never()).setReadOnly(anyBoolean());
	}

	@Test
	public void testFindAllStreamFetch()
	{
		String entityTypeId0 = "id0";
		EntityType entityType0 = createEntityTypeWithPermission(entityTypeId0, Permission.READ);
		String entityTypeId1 = "id1";
		EntityType entityType1 = createEntityTypeWithPermission(entityTypeId1, Permission.WRITE);
		Fetch fetch = mock(Fetch.class);
		Stream<Object> entityTypeIdStream = Stream.of(entityType0, entityType1);
		when(delegateRepository.findAll(entityTypeIdStream, fetch)).thenReturn(Stream.of(entityType0, entityType1));

		assertEquals(entityTypeRepositorySecurityDecorator.findAll(entityTypeIdStream, fetch).collect(toList()),
				asList(entityType0, entityType1));
		verify(entityType0).setReadOnly(true);
		verify(entityType1, never()).setReadOnly(anyBoolean());
	}

	private EntityType createEntityTypeWithPermission(String entityTypeId, Permission permission)
	{
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
		when(permissionService.hasPermissionOnEntityType(entityTypeId, permission)).thenReturn(true);
		return entityType0;
	}
}