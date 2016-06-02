package org.molgenis.security.user;

public class MolgenisUserDecoratorTest
{
	//	private Repository<Entity> decoratedRepository;
	//	private Repository<Entity> userAuthorityRepository;
	//	private MolgenisUserDecorator molgenisUserDecorator;
	//	private PasswordEncoder passwordEncoder;
	//
	//	@BeforeMethod
	//	public void setUp()
	//	{
	//		decoratedRepository = mock(Repository.class);
	//		userAuthorityRepository = mock(Repository.class);
	//		molgenisUserDecorator = new MolgenisUserDecorator(decoratedRepository, , );
	//		ApplicationContext ctx = mock(ApplicationContext.class);
	//		passwordEncoder = mock(PasswordEncoder.class);
	//		when(ctx.getBean(PasswordEncoder.class)).thenReturn(passwordEncoder);
	//		DataService dataService = mock(DataService.class);
	//		when(dataService.getRepository(UserAuthority.class.getSimpleName())).thenReturn(userAuthorityRepository);
	//		when(ctx.getBean(DataService.class)).thenReturn(dataService);
	//		new ApplicationContextProvider().setApplicationContext(ctx);
	//	}
	//
	//	@Test
	//	public void addEntity()
	//	{
	//		String password = "password";
	//		Entity entity = new MapEntity();
	//		entity.set(MolgenisUserMetaData.PASSWORD_, password);
	//		entity.set(MolgenisUserMetaData.SUPERUSER, false);
	//		molgenisUserDecorator.add(entity);
	//		verify(passwordEncoder).encode(password);
	//		verify(decoratedRepository).add(entity);
	//		verify(userAuthorityRepository, times(0)).add(any(UserAuthority.class));
	//	}
	//
	//	@SuppressWarnings("unchecked")
	//	@Test
	//	public void addStream()
	//	{
	//		String password = "password";
	//		Entity entity0 = new MapEntity();
	//		entity0.set(MolgenisUserMetaData.PASSWORD_, password);
	//		entity0.set(MolgenisUserMetaData.SUPERUSER, false);
	//		Entity entity1 = new MapEntity();
	//		entity1.set(MolgenisUserMetaData.PASSWORD_, password);
	//		entity1.set(MolgenisUserMetaData.SUPERUSER, false);
	//
	//		when(decoratedRepository.add(any(Stream.class))).thenAnswer(new Answer<Integer>()
	//		{
	//			@Override
	//			public Integer answer(InvocationOnMock invocation) throws Throwable
	//			{
	//				Stream<Entity> entities = (Stream<Entity>) invocation.getArguments()[0];
	//				List<Entity> entitiesList = entities.collect(Collectors.toList());
	//				return entitiesList.size();
	//			}
	//		});
	//		assertEquals(molgenisUserDecorator.add(Stream.of(entity0, entity1)), Integer.valueOf(2));
	//		verify(passwordEncoder, times(2)).encode(password);
	//		verify(userAuthorityRepository, times(0)).add(any(UserAuthority.class));
	//	}
	//
	//	@Test
	//	public void deleteStream()
	//	{
	//		Stream<Entity> entities = Stream.empty();
	//		molgenisUserDecorator.delete(entities);
	//		verify(decoratedRepository, times(1)).delete(entities);
	//	}
	//
	//	@SuppressWarnings(
	//	{ "unchecked", "rawtypes" })
	//	@Test
	//	public void updateStream()
	//	{
	//		Entity entity0 = mock(Entity.class);
	//		entity0.set(MolgenisUserMetaData.PASSWORD_, "password");
	//		Stream<Entity> entities = Stream.of(entity0);
	//		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
	//		doNothing().when(decoratedRepository).update(captor.capture());
	//		decoratedRepository.update(entities);
	//		assertEquals(captor.getValue().collect(Collectors.toList()), Arrays.asList(entity0));
	//		verify(entity0).set(eq(MolgenisUserMetaData.PASSWORD_), anyString());
	//		// TODO add authority tests
	//	}
	//
	//	@Test
	//	public void addEntitySu()
	//	{
	//		String password = "password";
	//		Entity entity = new MapEntity("id");
	//		entity.set("id", 1);
	//		entity.set(MolgenisUserMetaData.PASSWORD_, password);
	//		entity.set(MolgenisUserMetaData.SUPERUSER, true);
	//		when(decoratedRepository.findOneById(1)).thenReturn(entity);
	//
	//		molgenisUserDecorator.add(entity);
	//		verify(passwordEncoder).encode(password);
	//		verify(decoratedRepository).add(entity);
	//		// verify(userAuthorityRepository, times(1)).add(any(UserAuthority.class));
	//	}
	//
	//	@Test
	//	public void findOneObjectFetch()
	//	{
	//		Object id = Integer.valueOf(0);
	//		Fetch fetch = new Fetch();
	//		molgenisUserDecorator.findOneById(id, fetch);
	//		verify(decoratedRepository, times(1)).findOneById(id, fetch);
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
	//		when(decoratedRepository.findAll(entityIds)).thenReturn(Stream.of(entity0, entity1));
	//		Stream<Entity> expectedEntities = molgenisUserDecorator.findAll(entityIds);
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
	//		when(decoratedRepository.findAll(entityIds, fetch)).thenReturn(Stream.of(entity0, entity1));
	//		Stream<Entity> expectedEntities = molgenisUserDecorator.findAll(entityIds, fetch);
	//		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	//	}
	//
	//	@Test
	//	public void findAllAsStream()
	//	{
	//		Entity entity0 = mock(Entity.class);
	//		Query<Entity> query = mock(Query.class);
	//		when(decoratedRepository.findAll(query)).thenReturn(Stream.of(entity0));
	//		Stream<Entity> entities = molgenisUserDecorator.findAll(query);
	//		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0));
	//	}
	//
	//	@Test
	//	public void streamFetch()
	//	{
	//		Fetch fetch = new Fetch();
	//		molgenisUserDecorator.stream(fetch);
	//		verify(decoratedRepository, times(1)).stream(fetch);
	//	}
}
