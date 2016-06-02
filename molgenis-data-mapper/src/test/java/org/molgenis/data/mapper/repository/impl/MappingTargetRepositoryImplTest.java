package org.molgenis.data.mapper.repository.impl;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

/**
 * Unit test for the MappingTargetRepository. Tests the MappingTargetRepository in isolation.
 */
//@ContextConfiguration(classes =
//{ MappingTargetRepositoryImplTest.Config.class })
public class MappingTargetRepositoryImplTest extends AbstractTestNGSpringContextTests
{
	//	@Autowired
	//	private DataService dataService;
	//
	//	@Autowired
	//	private EntityMappingRepository entityMappingRepository;
	//
	//	@Autowired
	//	private MappingTargetRepositoryImpl mappingTargetRepository;
	//
	//	@Autowired
	//	private IdGenerator idGenerator;
	//
	//	private List<MappingTarget> mappingTargets;
	//
	//	private List<Entity> mappingTargetEntities;
	//
	//	private EntityMetaDataImpl targetEntityMetaData;
	//
	//	private List<Entity> entityMappingEntities;
	//
	//	private List<EntityMapping> entityMappings;
	//
	//	@Captor
	//	ArgumentCaptor<Collection<EntityMapping>> entityMappingCaptor;
	//
	//	@BeforeMethod
	//	public void beforeMethod()
	//	{
	//		MockitoAnnotations.initMocks(this);
	//
	//		// POJOs
	//		EntityMetaData sourceEntityMetaData = new EntityMetaDataImpl("source");
	//		targetEntityMetaData = new EntityMetaDataImpl("target");
	//		AttributeMetaData targetAttributeMetaData = new AttributeMetaData("targetAttribute");
	//		targetEntityMetaData.addAttribute(targetAttributeMetaData);
	//		entityMappings = Arrays.asList(new EntityMapping("entityMappingID", sourceEntityMetaData, targetEntityMetaData,
	//				emptyList()));
	//		mappingTargets = Arrays.asList(new MappingTarget("mappingTargetID", targetEntityMetaData, entityMappings));
	//
	//		// Entities
	//		Entity entityMappingEntity = new MapEntity(new EntityMappingMetaData());
	//		entityMappingEntity.set(EntityMappingMetaData.IDENTIFIER, "entityMappingID");
	//		entityMappingEntity.set(EntityMappingMetaData.SOURCEENTITYMETADATA, "source");
	//		entityMappingEntity.set(EntityMappingMetaData.TARGETENTITYMETADATA, "target");
	//		entityMappingEntity.set(EntityMappingMetaData.ATTRIBUTEMAPPINGS, emptyList());
	//		Entity mappingTargetEntity = new MapEntity(new MappingTargetMetaData());
	//		mappingTargetEntity.set(IDENTIFIER, "mappingTargetID");
	//		mappingTargetEntity.set(TARGET, "target");
	//
	//		entityMappingEntities = asList(entityMappingEntity);
	//		mappingTargetEntity.set(ENTITYMAPPINGS, entityMappingEntities);
	//
	//		mappingTargetEntities = asList(mappingTargetEntity);
	//	}
	//
	//	@Test
	//	public void testToMappingTargets()
	//	{
	//		when(dataService.getEntityMetaData("target")).thenReturn(targetEntityMetaData);
	//		when(entityMappingRepository.toEntityMappings(entityMappingEntities)).thenReturn(entityMappings);
	//		when(dataService.hasRepository("target")).thenReturn(true);
	//
	//		assertEquals(mappingTargetRepository.toMappingTargets(mappingTargetEntities), mappingTargets);
	//	}
	//
	//	@Test
	//	public void testUpdate()
	//	{
	//		when(entityMappingRepository.upsert(entityMappings)).thenReturn(entityMappingEntities);
	//		List<Entity> result = mappingTargetRepository.upsert(mappingTargets);
	//		assertEquals(result, mappingTargetEntities);
	//	}
	//
	//	@Test
	//	public void testInsert()
	//	{
	//		mappingTargets.get(0).setIdentifier(null);
	//
	//		when(idGenerator.generateId()).thenReturn("mappingTargetID");
	//		when(entityMappingRepository.upsert(entityMappings)).thenReturn(entityMappingEntities);
	//		List<Entity> result = mappingTargetRepository.upsert(mappingTargets);
	//		assertEquals(result, mappingTargetEntities);
	//	}
	//
	//	@Configuration
	//	public static class Config
	//	{
	//		@Bean
	//		DataServiceImpl dataService()
	//		{
	//			return mock(DataServiceImpl.class);
	//		}
	//
	//		@Bean
	//		EntityMappingRepository entityMappingRepository()
	//		{
	//			return mock(EntityMappingRepository.class);
	//		}
	//
	//		@Bean
	//		MappingTargetRepositoryImpl mappingTargetRepository()
	//		{
	//			return new MappingTargetRepositoryImpl(entityMappingRepository());
	//		}
	//
	//		@Bean
	//		MolgenisUserService userService()
	//		{
	//			return mock(MolgenisUserService.class);
	//		}
	//
	//		@Bean
	//		PermissionSystemService permissionSystemService()
	//		{
	//			return mock(PermissionSystemService.class);
	//		}
	//
	//		@Bean
	//		IdGenerator idGenerator()
	//		{
	//			return mock(IdGenerator.class);
	//		}
	//	}
}
