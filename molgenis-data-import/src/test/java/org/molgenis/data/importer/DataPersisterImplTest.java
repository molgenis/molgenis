package org.molgenis.data.importer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.importer.DataPersister.DataMode;
import org.molgenis.data.importer.DataPersister.MetadataMode;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.testng.Assert.assertEquals;

public class DataPersisterImplTest extends AbstractMockitoTest
{
	@Mock
	private MetaDataService metaDataService;
	private DataService dataService;
	@Mock
	private EntityTypeDependencyResolver entityTypeDependencyResolver;

	private DataPersisterImpl dataPersisterImpl;

	@Mock
	private DataProvider dataProvider;
	@Mock
	private EntityType entityType0;
	@Mock
	private EntityType entityType1;
	@Mock
	private EntityType entityType2;

	public DataPersisterImplTest()
	{
		super(Strictness.WARN);
	}

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		dataService = mock(DataService.class, RETURNS_DEEP_STUBS);
		dataPersisterImpl = new DataPersisterImpl(metaDataService, dataService, entityTypeDependencyResolver);

		String entityTypeId0 = "entityTypeId0";
		entityType0 = mock(EntityType.class);
		when(entityType0.getId()).thenReturn(entityTypeId0).getMock();
		String entityTypeId1 = "entityTypeId1";
		entityType1 = mock(EntityType.class);
		when(entityType1.getId()).thenReturn(entityTypeId1).getMock();
		String entityTypeId2 = "entityTypeId2";
		entityType2 = mock(EntityType.class);
		when(entityType2.getId()).thenReturn(entityTypeId2).getMock();

		when(dataProvider.getEntityTypes()).thenReturn(Stream.of(entityType0, entityType1, entityType2));

		when(dataProvider.hasEntities(entityType0)).thenReturn(true);
		Entity entity0a = mock(Entity.class);
		Entity entity0b = mock(Entity.class);
		when(dataProvider.getEntities(entityType0)).thenReturn(Stream.of(entity0a, entity0b));

		when(dataProvider.hasEntities(entityType1)).thenReturn(true);
		Entity entity1a = mock(Entity.class);
		Entity entity1b = mock(Entity.class);
		Entity entity1c = mock(Entity.class);
		when(dataProvider.getEntities(entityType1)).thenReturn(Stream.of(entity1a, entity1b, entity1c));

		when(dataProvider.hasEntities(entityType2)).thenReturn(false);

		when(entityTypeDependencyResolver.resolve(asList(entityType0, entityType1, entityType2))).thenReturn(
				asList(entityType2, entityType1, entityType0));

		doAnswer(invocation ->
		{
			Stream<Entity> entityStream = (Stream<Entity>) invocation.getArguments()[1];
			try
			{
				//noinspection ResultOfMethodCallIgnored
				entityStream.collect(toList());
			}
			catch (IllegalStateException ignored)
			{
			}
			return null;
		}).when(dataService).add(anyString(), any(Stream.class));
		doAnswer(invocation ->
		{
			Stream<Entity> entityStream = (Stream<Entity>) invocation.getArguments()[1];
			try
			{
				//noinspection ResultOfMethodCallIgnored
				entityStream.collect(toList());
			}
			catch (IllegalStateException ignored)
			{
			}
			return null;
		}).when(dataService).update(anyString(), any(Stream.class));
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testGenericDataPersisterImpl()
	{
		new DataPersisterImpl(null, null, null);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPersistMetaNoneDataAdd()
	{
		PersistResult persistResult = dataPersisterImpl.persist(dataProvider, MetadataMode.NONE, DataMode.ADD);
		assertEquals(persistResult,
				PersistResult.create(ImmutableMap.of(entityType0.getId(), 2L, entityType1.getId(), 3L)));

		InOrder inOrder = inOrder(metaDataService, dataService);
		// stream consumed, cannot verify content
		inOrder.verify(dataService).add(eq(entityType1.getId()), any(Stream.class));
		// stream consumed, cannot verify content
		inOrder.verify(dataService).add(eq(entityType0.getId()), any(Stream.class));

		verifyNoMoreInteractions(metaDataService, dataService);
	}

	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Abstract entity type 'Entity type #0' with id 'entityTypeId0' cannot contain entities")
	public void testPersistMetaNoneDataAddAbstractEntityType()
	{
		when(entityType0.getLabel()).thenReturn("Entity type #0");
		when(entityType0.isAbstract()).thenReturn(true);
		dataPersisterImpl.persist(dataProvider, MetadataMode.NONE, DataMode.ADD);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPersistMetaNoneDataUpdate()
	{
		PersistResult persistResult = dataPersisterImpl.persist(dataProvider, MetadataMode.NONE, DataMode.UPDATE);
		assertEquals(persistResult,
				PersistResult.create(ImmutableMap.of(entityType0.getId(), 2L, entityType1.getId(), 3L)));

		InOrder inOrder = inOrder(metaDataService, dataService);
		// stream consumed, cannot verify content
		inOrder.verify(dataService).update(eq(entityType1.getId()), any(Stream.class));
		// stream consumed, cannot verify content
		inOrder.verify(dataService).update(eq(entityType0.getId()), any(Stream.class));

		verifyNoMoreInteractions(metaDataService, dataService);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPersistMetaNoneDataUpsert()
	{
		Repository<Entity> repository0 = mock(Repository.class);
		when(dataService.getRepository(entityType0.getId())).thenReturn(repository0);
		Repository<Entity> repository1 = mock(Repository.class);
		when(dataService.getRepository(entityType1.getId())).thenReturn(repository1);

		PersistResult persistResult = dataPersisterImpl.persist(dataProvider, MetadataMode.NONE, DataMode.UPSERT);
		assertEquals(persistResult,
				PersistResult.create(ImmutableMap.of(entityType0.getId(), 2L, entityType1.getId(), 3L)));

		InOrder inOrder = inOrder(metaDataService, repository0, repository1);
		inOrder.verify(repository1).upsertBatch(anyList());
		inOrder.verify(repository0).upsertBatch(anyList());

		verifyNoMoreInteractions(metaDataService, repository0, repository1);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPersistMetaAddDataAdd()
	{
		PersistResult persistResult = dataPersisterImpl.persist(dataProvider, MetadataMode.ADD, DataMode.ADD);
		assertEquals(persistResult,
				PersistResult.create(ImmutableMap.of(entityType0.getId(), 2L, entityType1.getId(), 3L)));

		InOrder inOrder = inOrder(metaDataService, dataService);
		inOrder.verify(metaDataService).addEntityType(entityType2);
		inOrder.verify(metaDataService).addEntityType(entityType1);
		// stream consumed, cannot verify content
		inOrder.verify(dataService).add(eq(entityType1.getId()), any(Stream.class));
		inOrder.verify(metaDataService).addEntityType(entityType0);
		// stream consumed, cannot verify content
		inOrder.verify(dataService).add(eq(entityType0.getId()), any(Stream.class));

		verifyNoMoreInteractions(metaDataService, dataService);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPersistMetaAddDataAddMappedByAttributes()
	{
		when(entityType0.hasMappedByAttributes()).thenReturn(true);

		PersistResult persistResult = dataPersisterImpl.persist(dataProvider, MetadataMode.ADD, DataMode.ADD);
		assertEquals(persistResult,
				PersistResult.create(ImmutableMap.of(entityType0.getId(), 2L, entityType1.getId(), 3L)));

		InOrder inOrder = inOrder(metaDataService, dataService);
		inOrder.verify(metaDataService).addEntityType(entityType2);
		inOrder.verify(metaDataService).addEntityType(entityType1);
		// stream consumed, cannot verify content
		inOrder.verify(dataService).add(eq(entityType1.getId()), any(Stream.class));
		inOrder.verify(metaDataService)
			   .addEntityType(any(MetaDataServiceImpl.EntityTypeWithoutMappedByAttributes.class));
		// stream consumed, cannot verify content
		inOrder.verify(dataService).add(eq(entityType0.getId()), any(Stream.class));
		inOrder.verify(metaDataService).updateEntityType(entityType0);
		inOrder.verify(dataService).update(eq(entityType0.getId()), any(Stream.class));
		verifyNoMoreInteractions(metaDataService, dataService);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPersistMetaUpdateDataAdd()
	{
		EntityType existingEntityType0 = mock(EntityType.class);
		when(existingEntityType0.getId()).thenReturn("entityTypeId0");
		EntityType existingEntityType1 = mock(EntityType.class);
		when(existingEntityType1.getId()).thenReturn("entityTypeId1");
		EntityType existingEntityType2 = mock(EntityType.class);
		when(existingEntityType2.getId()).thenReturn("entityTypeId2");
		when(dataService.findOneById(ENTITY_TYPE_META_DATA, entityType0.getId(), EntityType.class)).thenReturn(
				existingEntityType0);
		when(dataService.findOneById(ENTITY_TYPE_META_DATA, entityType1.getId(), EntityType.class)).thenReturn(
				existingEntityType1);
		when(dataService.findOneById(ENTITY_TYPE_META_DATA, entityType2.getId(), EntityType.class)).thenReturn(
				existingEntityType2);
		when(dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)
						.in(EntityTypeMetadata.ID,
								Sets.newHashSet(entityType2.getId(), entityType1.getId(), entityType0.getId()))
						.findAll()).thenReturn(
				Stream.of(existingEntityType2, existingEntityType1, existingEntityType0));

		PersistResult persistResult = dataPersisterImpl.persist(dataProvider, MetadataMode.UPDATE, DataMode.ADD);
		assertEquals(persistResult,
				PersistResult.create(ImmutableMap.of(entityType0.getId(), 2L, entityType1.getId(), 3L)));

		InOrder inOrder = inOrder(metaDataService, dataService);
		inOrder.verify(metaDataService).updateEntityType(entityType2);
		inOrder.verify(metaDataService).updateEntityType(entityType1);
		inOrder.verify(dataService).add(eq(entityType1.getId()), any(Stream.class));
		inOrder.verify(metaDataService).updateEntityType(entityType0);
		inOrder.verify(dataService).add(eq(entityType0.getId()), any(Stream.class));

		// verifyNoMoreInteractions on dataService not possible due to data service reads
		verifyNoMoreInteractions(metaDataService);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPersistMetaUpsertDataAdd()
	{
		String entityType0Id = entityType0.getId();
		EntityType existingEntityType0 = mock(EntityType.class);
		when(existingEntityType0.getId()).thenReturn(entityType0Id);
		EntityType existingEntityType1 = mock(EntityType.class);
		String entityType1Id = entityType1.getId();
		when(existingEntityType1.getId()).thenReturn(entityType1Id);
		EntityType existingEntityType2 = mock(EntityType.class);
		String entityType2Id = entityType2.getId();
		when(existingEntityType2.getId()).thenReturn(entityType2Id);
		when(dataService.findOneById(ENTITY_TYPE_META_DATA, entityType0.getId(), EntityType.class)).thenReturn(null);
		when(dataService.findOneById(ENTITY_TYPE_META_DATA, entityType1.getId(), EntityType.class)).thenReturn(
				existingEntityType1);
		when(dataService.findOneById(ENTITY_TYPE_META_DATA, entityType2.getId(), EntityType.class)).thenReturn(
				existingEntityType2);
		when(dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)
						.in(EntityTypeMetadata.ID,
								Sets.newHashSet(entityType2.getId(), entityType1.getId(), entityType0.getId()))
						.findAll()).thenReturn(
				Stream.of(existingEntityType2, existingEntityType1, existingEntityType0));

		PersistResult persistResult = dataPersisterImpl.persist(dataProvider, MetadataMode.UPSERT, DataMode.ADD);
		assertEquals(persistResult,
				PersistResult.create(ImmutableMap.of(entityType0.getId(), 2L, entityType1.getId(), 3L)));

		InOrder inOrder = inOrder(metaDataService, dataService);
		inOrder.verify(metaDataService).updateEntityType(entityType2);
		inOrder.verify(metaDataService).updateEntityType(entityType1);
		inOrder.verify(dataService).add(eq(entityType1.getId()), any(Stream.class));
		inOrder.verify(metaDataService).addEntityType(entityType0);
		inOrder.verify(dataService).add(eq(entityType0.getId()), any(Stream.class));

		// verifyNoMoreInteractions on dataService not possible due to data service reads
		verifyNoMoreInteractions(metaDataService);
	}
}