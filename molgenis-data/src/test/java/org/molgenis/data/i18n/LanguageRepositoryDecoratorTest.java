package org.molgenis.data.i18n;

public class LanguageRepositoryDecoratorTest
{
	// FIXME
	//	private Repository<Entity> decoratedRepo;
	//	private DataService dataService;
	//	private LanguageRepositoryDecorator languageRepositoryDecorator;
	//	private LanguageMetaData languageMetaData;
	//
	//	@BeforeMethod
	//	public void setUpBeforeMethod()
	//	{
	//		decoratedRepo = mock(Repository.class);
	//		languageMetaData = mock(LanguageMetaData.class);
	//		when(decoratedRepo.getEntityMetaData()).thenReturn(languageMetaData);
	//		dataService = mock(DataService.class);
	//		MetaDataService metaDataService = mock(MetaDataService.class);
	//		RepositoryCollection defaultBackend = mock(RepositoryCollection.class);
	//		when(metaDataService.getDefaultBackend()).thenReturn(defaultBackend);
	//		when(dataService.getMeta()).thenReturn(metaDataService);
	//		languageRepositoryDecorator = new LanguageRepositoryDecorator(decoratedRepo, dataService);
	//	}
	//
	//	@Test
	//	public void addStream()
	//	{
	//		Entity entity0 = mock(Entity.class);
	//		when(entity0.getEntityMetaData()).thenReturn(languageMetaData);
	//		when(entity0.getString(LanguageMetaData.CODE)).thenReturn("nl");
	//
	//		Entity entity1 = mock(Entity.class);
	//		when(entity1.getEntityMetaData()).thenReturn(languageMetaData);
	//		when(entity1.getString(LanguageMetaData.CODE)).thenReturn("de");
	//
	//		Stream<Entity> entities = Arrays.asList(entity0, entity1).stream();
	//		assertEquals(languageRepositoryDecorator.add(entities), Integer.valueOf(2));
	//		verify(decoratedRepo, times(1)).add(entity0);
	//		verify(decoratedRepo, times(1)).add(entity1);
	//	}
	//
	//	@Test
	//	public void deleteStream()
	//	{
	//		Entity entity0 = mock(Entity.class);
	//		when(entity0.getEntityMetaData()).thenReturn(languageMetaData);
	//		when(entity0.getString(LanguageMetaData.CODE)).thenReturn("nl");
	//
	//		Entity entity1 = mock(Entity.class);
	//		when(entity1.getEntityMetaData()).thenReturn(languageMetaData);
	//		when(entity1.getString(LanguageMetaData.CODE)).thenReturn("de");
	//
	//		languageRepositoryDecorator.delete(Stream.of(entity0, entity1));
	//		verify(decoratedRepo, times(1)).delete(entity0);
	//		verify(decoratedRepo, times(1)).delete(entity1);
	//	}
	//
	//	@SuppressWarnings(
	//	{ "unchecked", "rawtypes" })
	//	@Test
	//	public void updateStream()
	//	{
	//		Entity entity0 = mock(Entity.class);
	//		Stream<Entity> entities = Stream.of(entity0);
	//		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
	//		doNothing().when(decoratedRepo).update(captor.capture());
	//		languageRepositoryDecorator.update(entities);
	//		assertEquals(captor.getValue().collect(Collectors.toList()), Arrays.asList(entity0));
	//	}
	//
	//	@Test
	//	public void findAllStream()
	//	{
	//		Object id0 = "id0";
	//		Object id1 = "id1";
	//		Entity entity0 = mock(Entity.class);
	//		Entity entity1 = mock(Entity.class);
	//		Stream<Object> entityIds = Stream.of(id0, id1);
	//		when(decoratedRepo.findAll(entityIds)).thenReturn(Stream.of(entity0, entity1));
	//		Stream<Entity> expectedEntities = languageRepositoryDecorator.findAll(entityIds);
	//		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	//	}
	//
	//	@Test
	//	public void findAllStreamFetch()
	//	{
	//		Fetch fetch = new Fetch();
	//		Object id0 = "id0";
	//		Object id1 = "id1";
	//		Entity entity0 = mock(Entity.class);
	//		Entity entity1 = mock(Entity.class);
	//		Stream<Object> entityIds = Stream.of(id0, id1);
	//		when(decoratedRepo.findAll(entityIds, fetch)).thenReturn(Stream.of(entity0, entity1));
	//		Stream<Entity> expectedEntities = languageRepositoryDecorator.findAll(entityIds, fetch);
	//		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	//	}
	//
	//	@Test
	//	public void findAllAsStream()
	//	{
	//		Entity entity0 = mock(Entity.class);
	//		Query<Entity> query = mock(Query.class);
	//		when(decoratedRepo.findAll(query)).thenReturn(Stream.of(entity0));
	//		Stream<Entity> entities = languageRepositoryDecorator.findAll(query);
	//		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0));
	//	}
	//
	//	@Test
	//	public void streamFetch()
	//	{
	//		Fetch fetch = new Fetch();
	//		languageRepositoryDecorator.stream(fetch);
	//		verify(decoratedRepo, times(1)).stream(fetch);
	//	}
}
