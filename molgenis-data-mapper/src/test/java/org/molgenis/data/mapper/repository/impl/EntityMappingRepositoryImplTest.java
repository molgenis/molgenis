package org.molgenis.data.mapper.repository.impl;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

//@ContextConfiguration(classes =
//{ EntityMappingRepositoryImplTest.Config.class, MappingConfig.class })
public class EntityMappingRepositoryImplTest extends AbstractTestNGSpringContextTests
{
	//	@Autowired
	//	private DataService dataService;
	//
	//	@Autowired
	//	private AttributeMappingRepositoryImpl attributeMappingRepository;
	//
	//	@Autowired
	//	private EntityMappingRepositoryImpl entityMappingRepository;
	//
	//	private static final String AUTO_ID = "1";
	//
	//	@Test
	//	public void testToEntityMappings()
	//	{
	//		AttributeMetaData targetAttributeMetaData = new AttributeMetaData("targetAttribute");
	//		List<AttributeMetaData> sourceAttributeMetaDatas = new ArrayList<AttributeMetaData>();
	//		EntityMetaData sourceEntityMetaData = new EntityMetaDataImpl("source");
	//		EntityMetaData targetEntityMetaData = new EntityMetaDataImpl("target");
	//		targetEntityMetaData.addAttribute(targetAttributeMetaData);
	//
	//		List<AttributeMapping> attributeMappings = new ArrayList<AttributeMapping>();
	//		attributeMappings
	//				.add(new AttributeMapping("1", targetAttributeMetaData, "algorithm", sourceAttributeMetaDatas));
	//
	//		List<EntityMapping> entityMappings = Arrays.asList(new EntityMapping(AUTO_ID, sourceEntityMetaData,
	//				targetEntityMetaData, attributeMappings));
	//
	//		Entity attributeMappingEntity = new MapEntity(new AttributeMappingMetaData());
	//		attributeMappingEntity.set(EntityMappingMetaData.IDENTIFIER, AUTO_ID);
	//		attributeMappingEntity.set(AttributeMappingMetaData.TARGETATTRIBUTEMETADATA, "targetAttribute");
	//		attributeMappingEntity.set(AttributeMappingMetaData.SOURCEATTRIBUTEMETADATAS, "sourceAttributes");
	//		attributeMappingEntity.set(AttributeMappingMetaData.ALGORITHM, "algorithm");
	//
	//		List<Entity> attributeMappingEntities = new ArrayList<Entity>();
	//		attributeMappingEntities.add(attributeMappingEntity);
	//
	//		List<Entity> entityMappingEntities = new ArrayList<Entity>();
	//		Entity entityMappingEntity = new MapEntity(new EntityMappingMetaData());
	//		entityMappingEntity.set(EntityMappingMetaData.IDENTIFIER, AUTO_ID);
	//		entityMappingEntity.set(EntityMappingMetaData.TARGET_ENTITY_META_DATA, "targetAttribute");
	//		entityMappingEntity.set(AttributeMappingMetaData.ALGORITHM, "algorithm");
	//		entityMappingEntity.set(EntityMappingMetaData.ATTRIBUTE_MAPPINGS, attributeMappingEntities);
	//
	//		entityMappingEntities.add(entityMappingEntity);
	//
	//		when(dataService.getEntityMetaData(entityMappingEntity.getString(EntityMappingMetaData.TARGET_ENTITY_META_DATA)))
	//				.thenReturn(targetEntityMetaData);
	//		when(dataService.getEntityMetaData(entityMappingEntity.getString(EntityMappingMetaData.SOURCE_ENTITY_META_DATA)))
	//				.thenReturn(sourceEntityMetaData);
	//
	//		assertEquals(entityMappingRepository.toEntityMappings(entityMappingEntities), entityMappings);
	//	}
	//
	//	@Test
	//	public void testUpsert()
	//	{
	//		AttributeMetaData targetAttributeMetaData = new AttributeMetaData("targetAttribute");
	//		List<AttributeMetaData> sourceAttributeMetaDatas = new ArrayList<AttributeMetaData>();
	//		EntityMetaData sourceEntityMetaData = new EntityMetaDataImpl("source");
	//		EntityMetaData targetEntityMetaData = new EntityMetaDataImpl("target");
	//		targetEntityMetaData.addAttribute(targetAttributeMetaData);
	//
	//		List<AttributeMapping> attributeMappings = new ArrayList<AttributeMapping>();
	//		attributeMappings
	//				.add(new AttributeMapping("1", targetAttributeMetaData, "algorithm", sourceAttributeMetaDatas));
	//
	//		Collection<EntityMapping> entityMappings = Arrays.asList(new EntityMapping(AUTO_ID, sourceEntityMetaData,
	//				targetEntityMetaData, attributeMappings));
	//
	//		Entity attributeMappingEntity = new MapEntity(new AttributeMappingMetaData());
	//		attributeMappingEntity.set(EntityMappingMetaData.IDENTIFIER, AUTO_ID);
	//		attributeMappingEntity.set(AttributeMappingMetaData.TARGETATTRIBUTEMETADATA, "targetAttribute");
	//		attributeMappingEntity.set(AttributeMappingMetaData.SOURCEATTRIBUTEMETADATAS, sourceAttributeMetaDatas);
	//		attributeMappingEntity.set(AttributeMappingMetaData.ALGORITHM, "algorithm");
	//		attributeMappingEntity.set(AttributeMappingMetaData.ALGORITHMSTATE, null);
	//
	//		List<Entity> attributeMappingEntities = new ArrayList<Entity>();
	//		attributeMappingEntities.add(attributeMappingEntity);
	//
	//		List<Entity> entityMappingEntities = new ArrayList<Entity>();
	//		Entity entityMappingEntity = new MapEntity(new EntityMappingMetaData());
	//		entityMappingEntity.set(EntityMappingMetaData.IDENTIFIER, AUTO_ID);
	//		entityMappingEntity.set(EntityMappingMetaData.SOURCE_ENTITY_META_DATA, "source");
	//		entityMappingEntity.set(EntityMappingMetaData.TARGET_ENTITY_META_DATA, "target");
	//		entityMappingEntity.set(EntityMappingMetaData.ATTRIBUTE_MAPPINGS, attributeMappingEntities);
	//		entityMappingEntities.add(entityMappingEntity);
	//
	//		assertEquals(entityMappingRepository.upsert(entityMappings).get(0), entityMappingEntities.get(0));
	//
	//		verify(dataService).update(new EntityMappingMetaData().getName(), entityMappingEntity);
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
	//		SemanticSearchService semanticSearchService()
	//		{
	//			return mock(SemanticSearchService.class);
	//		}
	//
	//		@Bean
	//		AttributeMappingRepositoryImpl attributeMappingRepository()
	//		{
	//			return new AttributeMappingRepositoryImpl(dataService(), new AttributeMappingMetaData());
	//		}
	//
	//		@Bean
	//		EntityMappingRepositoryImpl entityMappingRepository()
	//		{
	//			return new EntityMappingRepositoryImpl(attributeMappingRepository());
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
	//			return new UuidGenerator();
	//		}
	//
	//		@Bean
	//		public OntologyTagService ontologyTagService()
	//		{
	//			return mock(OntologyTagService.class);
	//		}
	//	}
}
