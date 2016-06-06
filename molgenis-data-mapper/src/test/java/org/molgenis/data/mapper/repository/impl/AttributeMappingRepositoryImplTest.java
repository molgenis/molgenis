package org.molgenis.data.mapper.repository.impl;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

//@ContextConfiguration(classes = { AttributeMappingRepositoryImplTest.Config.class, MappingConfig.class,
//		OntologyConfig.class })
public class AttributeMappingRepositoryImplTest extends AbstractTestNGSpringContextTests
{
	//	@Autowired
	//	private DataService dataService;
	//
	//	@Autowired
	//	private AttributeMappingRepositoryImpl attributeMappingRepository;
	//
	//	@Autowired
	//	private IdGenerator idGenerator;
	//
	//	@Test
	//	public void testGetAttributeMappings()
	//	{
	//		AttributeMetaData targetAttributeMetaData = new AttributeMetaData("targetAttribute");
	//		List<AttributeMetaData> sourceAttributeMetaDatas = new ArrayList<AttributeMetaData>();
	//
	//		List<AttributeMapping> attributeMappings = new ArrayList<AttributeMapping>();
	//		attributeMappings.add(new AttributeMapping("attributeMappingID", targetAttributeMetaData, "algorithm",
	//				sourceAttributeMetaDatas));
	//
	//		Entity attributeMappingEntity = null;// FIXME new MapEntity(new AttributeMappingMetaData());
	//		attributeMappingEntity.set(IDENTIFIER, "attributeMappingID");
	//		attributeMappingEntity.set(TARGETATTRIBUTEMETADATA, "targetAttribute");
	//		attributeMappingEntity.set(SOURCEATTRIBUTEMETADATAS, "sourceAttributes");
	//		attributeMappingEntity.set(ALGORITHM, "algorithm");
	//
	//		List<Entity> attributeMappingEntities = new ArrayList<Entity>();
	//		attributeMappingEntities.add(attributeMappingEntity);
	//
	//		EntityMetaData sourceEntityMetaData = new EntityMetaData("source");
	//		EntityMetaData targetEntityMetaData = new EntityMetaData("target");
	//		targetEntityMetaData.addAttribute(targetAttributeMetaData);
	//
	//		assertEquals(attributeMappingRepository
	//						.getAttributeMappings(attributeMappingEntities, sourceEntityMetaData, targetEntityMetaData),
	//				attributeMappings);
	//	}
	//
	//	@Test
	//	public void testUpdate()
	//	{
	//		AttributeMetaData targetAttributeMetaData = new AttributeMetaData("targetAttribute");
	//		List<AttributeMetaData> sourceAttributeMetaDatas = new ArrayList<AttributeMetaData>();
	//
	//		targetAttributeMetaData.setDataType(MolgenisFieldTypes.STRING);
	//
	//		Collection<AttributeMapping> attributeMappings = Arrays
	//				.asList(new AttributeMapping("attributeMappingID", targetAttributeMetaData, "algorithm",
	//						sourceAttributeMetaDatas));
	//
	//		List<Entity> result = new ArrayList<Entity>();
	//		Entity attributeMappingEntity = null; // FIXME new MapEntity(new AttributeMappingMetaData());
	//		attributeMappingEntity.set(IDENTIFIER, "attributeMappingID");
	//		attributeMappingEntity.set(TARGETATTRIBUTEMETADATA, targetAttributeMetaData.getName());
	//		attributeMappingEntity.set(SOURCEATTRIBUTEMETADATAS, sourceAttributeMetaDatas);
	//		attributeMappingEntity.set(ALGORITHM, "algorithm");
	//		attributeMappingEntity.set(ALGORITHMSTATE, null);
	//
	//		result.add(attributeMappingEntity);
	//
	//		assertEquals(attributeMappingRepository.upsert(attributeMappings), result);
	//
	//		//		verify(dataService).update(AttributeMappingRepositoryImpl.META_DATA.getName(), attributeMappingEntity); // FIXME
	//	}
	//
	//	@Test
	//	public void testInsert()
	//	{
	//		AttributeMetaData targetAttributeMetaData = new AttributeMetaData("targetAttribute");
	//		List<AttributeMetaData> sourceAttributeMetaDatas = new ArrayList<AttributeMetaData>();
	//		targetAttributeMetaData.setDataType(MolgenisFieldTypes.STRING);
	//
	//		Collection<AttributeMapping> attributeMappings = Arrays
	//				.asList(new AttributeMapping(null, targetAttributeMetaData, "algorithm", sourceAttributeMetaDatas));
	//
	//		Mockito.when(idGenerator.generateId()).thenReturn("attributeMappingID");
	//
	//		List<Entity> result = new ArrayList<Entity>();
	//		Entity attributeMappingEntity = null; // FIXME new MapEntity(new AttributeMappingMetaData());
	//		attributeMappingEntity.set(IDENTIFIER, "attributeMappingID");
	//		attributeMappingEntity.set(TARGETATTRIBUTEMETADATA, targetAttributeMetaData.getName());
	//		attributeMappingEntity.set(SOURCEATTRIBUTEMETADATAS, sourceAttributeMetaDatas);
	//		attributeMappingEntity.set(ALGORITHM, "algorithm");
	//		attributeMappingEntity.set(ALGORITHMSTATE, null);
	//
	//		result.add(attributeMappingEntity);
	//
	//		assertEquals(attributeMappingRepository.upsert(attributeMappings), result);
	//
	//		//		verify(dataService).add(META_DATA.getName(), attributeMappingEntity); // FIXME
	//	}
	//
	//	@Test
	//	public void testRetrieveAttributeMetaDatasFromAlgorithm()
	//	{
	//		String algorithm = "$('attribute_1').value()$('attribute_2').value()";
	//
	//		AttributeMetaData attr1 = new AttributeMetaData("attribute_1");
	//		AttributeMetaData attr2 = new AttributeMetaData("attribute_2");
	//
	//		EntityMetaData sourceEntityMetaData = new EntityMetaData("source");
	//		sourceEntityMetaData.addAttribute(attr1);
	//		sourceEntityMetaData.addAttribute(attr2);
	//
	//		List<AttributeMetaData> sourceAttributeMetaDatas = new ArrayList<AttributeMetaData>();
	//		sourceAttributeMetaDatas.add(attr1);
	//		sourceAttributeMetaDatas.add(attr2);
	//
	//		assertEquals(
	//				attributeMappingRepository.retrieveAttributeMetaDatasFromAlgorithm(algorithm, sourceEntityMetaData),
	//				sourceAttributeMetaDatas);
	//	}
	//
	//	@Configuration
	//	public static class Config
	//	{
	//		@Bean
	//		DataService dataService()
	//		{
	//			return mock(DataService.class);
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
	//			return new AttributeMappingRepositoryImpl(dataService(), null); // FIXME
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
	//			IdGenerator idGenerator = mock(IdGenerator.class);
	//			return idGenerator;
	//		}
	//
	//		@Bean
	//		public OntologyTagService ontologyTagService()
	//		{
	//			return mock(OntologyTagService.class);
	//		}
	//	}
}
