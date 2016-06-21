package org.molgenis.data.mapper.repository.impl;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

//@ContextConfiguration(classes = MappingProjectRepositoryImplTest.Config.class)
public class MappingProjectRepositoryImplTest extends AbstractTestNGSpringContextTests
{
	//	@Autowired
	//	private MappingProjectRepositoryImpl mappingProjectRepositoryImpl;
	//
	//	@Autowired
	//	private DataService dataService;
	//
	//	@Autowired
	//	private MappingTargetRepository mappingTargetRepository;
	//
	//	@Autowired
	//	private IdGenerator idGenerator;
	//
	//	@Autowired
	//	private MolgenisUserService userService;
	//
	//	private MolgenisUser owner;
	//
	//	private MappingTarget mappingTarget1;
	//
	//	private MappingTarget mappingTarget2;
	//
	//	private List<Entity> mappingTargetEntities;
	//
	//	private MappingProject mappingProject;
	//
	//	private Entity mappingProjectEntity;
	//
	//	@BeforeMethod
	//	public void beforeMethod()
	//	{
	//		owner = new MolgenisUser();
	//		owner.setUsername("flup");
	//		owner.setPassword("geheim");
	//		owner.setId("12345");
	//		owner.setActive(true);
	//		owner.setEmail("flup@blah.com");
	//		owner.setFirstName("Flup");
	//		owner.setLastName("de Flap");
	//
	//		EntityMetaDataImpl target1 = new EntityMetaDataImpl("target1");
	//		target1.addAttribute("id", ROLE_ID);
	//		EntityMetaDataImpl target2 = new EntityMetaDataImpl("target2");
	//		target2.addAttribute("id", ROLE_ID);
	//
	//		mappingProject = new MappingProject("My first mapping project", owner);
	//		mappingTarget1 = mappingProject.addTarget(target1);
	//		mappingTarget2 = mappingProject.addTarget(target2);
	//
	//		Entity mappingTargetEntity = new MapEntity(new MappingTargetMetaData());
	//		mappingTargetEntity.set(MappingTargetMetaData.TARGET, "target1");
	//		mappingTargetEntity.set(MappingTargetMetaData.IDENTIFIER, "mappingTargetID1");
	//		Entity mappingTargetEntity2 = new MapEntity(new MappingTargetMetaData());
	//		mappingTargetEntity2.set(MappingTargetMetaData.TARGET, "target2");
	//		mappingTargetEntity2.set(MappingTargetMetaData.IDENTIFIER, "mappingTargetID2");
	//		mappingTargetEntities = asList(mappingTargetEntity, mappingTargetEntity2);
	//
	//		mappingProjectEntity = new MapEntity(new MappingProjectMetaData());
	//		mappingProjectEntity.set(IDENTIFIER, "mappingProjectID");
	//		mappingProjectEntity.set(MAPPING_TARGETS, mappingTargetEntities);
	//		mappingProjectEntity.set(OWNER, owner);
	//		mappingProjectEntity.set(NAME, "My first mapping project");
	//	}
	//
	//	@Test
	//	public void testAdd()
	//	{
	//		when(idGenerator.generateId()).thenReturn("mappingProjectID");
	//		when(mappingTargetRepository.upsert(asList(mappingTarget1, mappingTarget2))).thenReturn(mappingTargetEntities);
	//
	//		mappingProjectRepositoryImpl.add(mappingProject);
	//
	//		Mockito.verify(dataService).add(TAG, mappingProjectEntity);
	//		assertNull(mappingTarget1.getIdentifier());
	//		assertNull(mappingTarget2.getIdentifier());
	//	}
	//
	//	@Test
	//	public void testAddWithIdentifier()
	//	{
	//		MappingProject mappingProject = new MappingProject("My first mapping project", owner);
	//		mappingProject.setIdentifier("mappingProjectID");
	//		try
	//		{
	//			mappingProjectRepositoryImpl.add(mappingProject);
	//		}
	//		catch (MolgenisDataException mde)
	//		{
	//			assertEquals(mde.getMessage(), "MappingProject already exists");
	//		}
	//	}
	//
	//	@Test
	//	public void testDelete()
	//	{
	//		mappingProjectRepositoryImpl.delete("abc");
	//		verify(dataService).deleteById(TAG, "abc");
	//	}
	//
	//	@Test
	//	public void testQuery()
	//	{
	//		Query<Entity> q = new QueryImpl<>();
	//		q.eq(OWNER, "flup");
	//		when(dataService.findAll(TAG, q)).thenReturn(Stream.of(mappingProjectEntity));
	//		when(userService.getUser("flup")).thenReturn(owner);
	//		when(mappingTargetRepository.toMappingTargets(mappingTargetEntities))
	//				.thenReturn(asList(mappingTarget1, mappingTarget2));
	//		List<MappingProject> result = mappingProjectRepositoryImpl.getMappingProjects(q);
	//		mappingProject.setIdentifier("mappingProjectID");
	//		assertEquals(result, asList(mappingProject));
	//	}
	//
	//	@Test
	//	public void testFindAll()
	//	{
	//		Query<Entity> q = new QueryImpl<Entity>();
	//		q.eq(OWNER, "flup");
	//		when(dataService.findAll(TAG)).thenReturn(Stream.of(mappingProjectEntity));
	//		when(userService.getUser("flup")).thenReturn(owner);
	//		when(mappingTargetRepository.toMappingTargets(mappingTargetEntities))
	//				.thenReturn(asList(mappingTarget1, mappingTarget2));
	//		List<MappingProject> result = mappingProjectRepositoryImpl.getAllMappingProjects();
	//		mappingProject.setIdentifier("mappingProjectID");
	//		assertEquals(result, asList(mappingProject));
	//	}
	//
	//	@Test
	//	public void testUpdateUnknown()
	//	{
	//		mappingProject.setIdentifier("mappingProjectID");
	//		when(dataService.findOneById(TAG, "mappingProjectID")).thenReturn(null);
	//		try
	//		{
	//			mappingProjectRepositoryImpl.update(mappingProject);
	//			fail("Expected exception");
	//		}
	//		catch (MolgenisDataException expected)
	//		{
	//			assertEquals(expected.getMessage(), "MappingProject does not exist");
	//		}
	//	}
	//
	//	@Configuration
	//	public static class Config
	//	{
	//		@Bean
	//		public DataService dataService()
	//		{
	//			return mock(DataService.class);
	//		}
	//
	//		@Bean
	//		public MappingTargetRepository mappingTargetRepository()
	//		{
	//			return mock(MappingTargetRepository.class);
	//		}
	//
	//		@Bean
	//		public MolgenisUserService molgenisUserService()
	//		{
	//			return mock(MolgenisUserService.class);
	//		}
	//
	//		@Bean
	//		public IdGenerator idGenerator()
	//		{
	//			return mock(IdGenerator.class);
	//		}
	//
	//		@Bean
	//		public MappingProjectRepositoryImpl mappingProjectRepositoryImpl()
	//		{
	//			return new MappingProjectRepositoryImpl(dataService(), mappingTargetRepository());
	//		}
	//
	//	}
}
