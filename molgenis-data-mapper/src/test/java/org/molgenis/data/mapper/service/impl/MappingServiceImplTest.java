package org.molgenis.data.mapper.service.impl;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

//@ContextConfiguration(classes =
//{ MappingServiceImplTest.Config.class, MappingConfig.class })
public class MappingServiceImplTest extends AbstractTestNGSpringContextTests
{
	//	@Autowired
	//	private RepositoryCollection repoCollection;
	//
	//	@Autowired
	//	private DataServiceImpl dataService;
	//
	//	@Autowired
	//	private MappingService mappingService;
	//
	//	@Autowired
	//	private MolgenisUserService userService;
	//
	//	@Autowired
	//	private PermissionSystemService permissionSystemService;
	//
	//	@Autowired
	//	private IdGenerator idGenerator;
	//
	//	@Autowired
	//	private MappingProjectRepository mappingProjectRepository;
	//
	//	private MolgenisUser user;
	//
	//	private EntityMetaDataImpl hopMetaData;
	//
	//	private EntityMetaDataImpl geneMetaData;
	//
	//	private EntityMetaData exonMetaData;
	//
	//	private final UuidGenerator uuidGenerator = new UuidGenerator();
	//
	//	@BeforeMethod
	//	public void beforeMethod()
	//	{
	//		user = new MolgenisUser();
	//		user.setUsername("Piet");
	//		when(userService.getUser("Piet")).thenReturn(user);
	//
	//		hopMetaData = new EntityMetaDataImpl("HopEntity", new Package("")); // FIXME Package.defaultPackage);
	//		hopMetaData.addAttribute("identifier", ROLE_ID);
	//		hopMetaData.addAttribute("height").setDataType(DECIMAL).setNillable(false);
	//
	//		geneMetaData = new EntityMetaDataImpl("Gene", new Package("")); // FIXME Package.defaultPackage);
	//		geneMetaData.addAttribute("id", ROLE_ID);
	//		geneMetaData.addAttribute("length").setDataType(DECIMAL).setNillable(false);
	//
	//		exonMetaData = new EntityMetaDataImpl("Exon", new Package("")); // FIXME Package.defaultPackage);
	//		exonMetaData.addAttribute("id", ROLE_ID);
	//		exonMetaData.addAttribute("basepairs").setDataType(DECIMAL).setNillable(false);
	//
	//		if (!dataService.hasRepository("HopEntity"))
	//		{
	//			dataService.getMeta().addEntityMeta(hopMetaData);
	//		}
	//
	//		// add 3 Gene entities
	//		Repository<Entity> gene = dataService.getMeta().addEntityMeta(geneMetaData);
	//		gene.deleteAll(); // refresh
	//		for (int i = 1; i < 4; i++)
	//		{
	//			MapEntity geneEntity = new MapEntity(geneMetaData);
	//			geneEntity.set("id", Integer.valueOf(i).toString());
	//			geneEntity.set("length", i * 2);
	//			gene.add(geneEntity);
	//		}
	//
	//		// add 1 Exon entity
	//		Repository<Entity> exon = dataService.getMeta().addEntityMeta(exonMetaData);
	//		exon.deleteAll(); // refresh
	//		MapEntity geneEntity = new MapEntity(exonMetaData);
	//		geneEntity.set("id", "A");
	//		geneEntity.set("basepairs", new Double(12345));
	//		exon.add(geneEntity);
	//
	//		dataService.getEntityNames().forEach(dataService::removeRepository);
	//
	//		TestingAuthenticationToken authentication = new TestingAuthenticationToken("userName", null);
	//		authentication.setAuthenticated(false);
	//		SecurityContextHolder.getContext().setAuthentication(authentication);
	//
	//	}
	//
	//	@Test
	//	public void testAddMappingProject()
	//	{
	//		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());
	//
	//		MappingProject added = mappingService.addMappingProject("Test123", user, "HopEntity");
	//		assertEquals(added.getName(), "Test123");
	//
	//		MappingProject expected = new MappingProject("Test123", user);
	//		expected.addTarget(hopMetaData);
	//
	//		final String mappingProjectId = added.getIdentifier();
	//		assertNotNull(mappingProjectId);
	//		expected.setIdentifier(mappingProjectId);
	//
	//		final String mappingTargetId = added.getMappingTarget("HopEntity").getIdentifier();
	//		assertNotNull(mappingTargetId);
	//		expected.getMappingTarget("HopEntity").setIdentifier(mappingTargetId);
	//		assertEquals(added, expected);
	//
	//		MappingProject retrieved = mappingService.getMappingProject(mappingProjectId);
	//		assertEquals(retrieved, expected);
	//	}
	//
	//	// TODO add unit test for testCloneMappingProject when InMemoryRepositoryCollection supports Query.
	//	@Test
	//	public void testCloneMappingProjectString()
	//	{
	//		when(idGenerator.generateId()).thenReturn("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
	//
	//		MappingProject mappingProject = createMappingProjectWithMappings("testCloneMappingProject");
	//		mappingService.updateMappingProject(mappingProject);
	//
	//		MappingProject clonedMappingProject = mappingService.cloneMappingProject(mappingProject.getIdentifier(),
	//				"Clone of TestRun");
	//
	//		List<MappingTarget> mappingTargets = mappingProject.getMappingTargets();
	//		List<MappingTarget> clonedMappingTargets = clonedMappingProject.getMappingTargets();
	//		assertEquals(mappingTargets.size(), clonedMappingTargets.size());
	//		for (int i = 0; i < mappingTargets.size(); ++i)
	//		{
	//			MappingTarget mappingTarget = mappingTargets.get(i);
	//			MappingTarget clonedMappingTarget = clonedMappingTargets.get(i);
	//
	//			assertNotEquals(mappingTarget.getIdentifier(), clonedMappingTarget.getIdentifier());
	//			assertEquals(mappingTarget.getTarget().getName(), clonedMappingTarget.getTarget().getName());
	//
	//			List<EntityMapping> entityMappings = Lists.newArrayList(mappingTarget.getEntityMappings());
	//			List<EntityMapping> clonedEntityMappings = Lists.newArrayList(clonedMappingTarget.getEntityMappings());
	//			assertEquals(entityMappings.size(), clonedEntityMappings.size());
	//
	//			for (int j = 0; j < entityMappings.size(); ++j)
	//			{
	//				EntityMapping entityMapping = entityMappings.get(j);
	//				EntityMapping clonedEntityMapping = clonedEntityMappings.get(j);
	//
	//				assertNotEquals(entityMapping.getIdentifier(), clonedEntityMapping.getIdentifier());
	//				assertEquals(entityMapping.getLabel(), clonedEntityMapping.getLabel());
	//				assertEquals(entityMapping.getName(), clonedEntityMapping.getName());
	//				assertEquals(entityMapping.getSourceEntityMetaData().getName(),
	//						clonedEntityMapping.getSourceEntityMetaData().getName());
	//				assertEquals(entityMapping.getTargetEntityMetaData().getName(),
	//						clonedEntityMapping.getTargetEntityMetaData().getName());
	//
	//				List<AttributeMapping> attributeMappings = Lists.newArrayList(entityMapping.getAttributeMappings());
	//				List<AttributeMapping> clonedAttributeMappings = Lists
	//						.newArrayList(clonedEntityMapping.getAttributeMappings());
	//				assertEquals(attributeMappings.size(), clonedAttributeMappings.size());
	//
	//				for (int k = 0; k < attributeMappings.size(); ++k)
	//				{
	//					AttributeMapping attributeMapping = attributeMappings.get(k);
	//					AttributeMapping clonedAttributeMapping = clonedAttributeMappings.get(k);
	//					assertNotEquals(attributeMapping.getIdentifier(), clonedAttributeMapping.getIdentifier());
	//
	//					assertEquals(attributeMapping.getAlgorithm(), clonedAttributeMapping.getAlgorithm());
	//					assertEquals(attributeMapping.getTargetAttributeMetaData().getName(),
	//							clonedAttributeMapping.getTargetAttributeMetaData().getName());
	//				}
	//			}
	//		}
	//	}
	//
	//	@Test
	//	public void testAddTarget()
	//	{
	//		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());
	//
	//		MappingProject mappingProject = mappingService.addMappingProject("Test123", user, "HopEntity");
	//		mappingProject.addTarget(geneMetaData);
	//
	//		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());
	//		mappingService.updateMappingProject(mappingProject);
	//
	//		MappingProject retrieved = mappingService.getMappingProject(mappingProject.getIdentifier());
	//		assertEquals(mappingProject, retrieved);
	//	}
	//
	//	@Test(expectedExceptions = IllegalStateException.class)
	//	public void testAddExistingTarget()
	//	{
	//		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());
	//
	//		MappingProject mappingProject = mappingService.addMappingProject("Test123", user, "HopEntity");
	//		mappingProject.addTarget(hopMetaData);
	//	}
	//
	//	@Test
	//	public void testAddNewSource()
	//	{
	//		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());
	//
	//		MappingProject mappingProject = mappingService.addMappingProject("Test123", user, "HopEntity");
	//
	//		// now add new source
	//		EntityMapping mapping = mappingProject.getMappingTarget("HopEntity").addSource(geneMetaData);
	//		mappingService.updateMappingProject(mappingProject);
	//
	//		MappingProject retrieved = mappingService.getMappingProject(mappingProject.getIdentifier());
	//		assertEquals(retrieved, mappingProject);
	//
	//		assertEquals(retrieved.getMappingTarget("HopEntity").getMappingForSource("Gene"), mapping);
	//	}
	//
	//	@Test
	//	public void testAddExistingSource()
	//	{
	//		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());
	//
	//		MappingProject mappingProject = mappingService.addMappingProject("Test123", user, "HopEntity");
	//		mappingProject.getMappingTarget("HopEntity").addSource(geneMetaData);
	//
	//		mappingService.updateMappingProject(mappingProject);
	//		MappingProject retrieved = mappingService.getMappingProject(mappingProject.getIdentifier());
	//		try
	//		{
	//			retrieved.getMappingTarget("HopEntity").addSource(geneMetaData);
	//			fail("Expected exception");
	//		}
	//		catch (IllegalStateException expected)
	//		{
	//		}
	//	}
	//
	//	@Test
	//	public void testApplyMappings()
	//	{
	//		String entityName = "Koetjeboe";
	//		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());
	//		createMappingProjectWithMappings(entityName);
	//
	//		Repository<Entity> actual = dataService.getRepository(entityName);
	//		EntityMetaData expectedMetadata = EntityMetaDataImpl.newInstance(hopMetaData);
	//		expectedMetadata.setName(entityName);
	//		expectedMetadata.addAttribute("source");
	//		assertEquals(actual.getEntityMetaData(), expectedMetadata);
	//		Set<Entity> created = Sets.newHashSet(actual.iterator());
	//
	//		MapEntity koetje1 = new MapEntity(expectedMetadata);
	//		koetje1.set("identifier", "1");
	//		koetje1.set("height", new Double(2));
	//		koetje1.set("source", "Gene");
	//		MapEntity koetje2 = new MapEntity(expectedMetadata);
	//		koetje2.set("identifier", "2");
	//		koetje2.set("height", new Double(4));
	//		koetje2.set("source", "Gene");
	//		MapEntity koetje3 = new MapEntity(expectedMetadata);
	//		koetje3.set("identifier", "3");
	//		koetje3.set("height", new Double(6));
	//		koetje3.set("source", "Gene");
	//
	//		assertEquals(created, ImmutableSet.<Entity> of(koetje1, koetje2, koetje3));
	//		verify(permissionSystemService).giveUserEntityPermissions(SecurityContextHolder.getContext(),
	//				Arrays.asList(entityName));
	//	}
	//
	//	/**
	//	 * New entities in the source should be added to the target when a new mapping to the same target is performed.
	//	 */
	//	@Test
	//	public void testAdd()
	//	{
	//		String entityName = "addEntity";
	//		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());
	//
	//		// make project and apply mappings once
	//		MappingProject project = createMappingProjectWithMappings(entityName);
	//
	//		// add an entity to the source
	//		MapEntity geneEntity = new MapEntity(geneMetaData);
	//		geneEntity.set("id", "4");
	//		geneEntity.set("length", new Double(8));
	//		dataService.add(geneMetaData.getName(), geneEntity);
	//
	//		// apply mapping again
	//		mappingService.applyMappings(project.getMappingTarget("HopEntity"), entityName);
	//
	//		Repository<Entity> actual = dataService.getRepository(entityName);
	//		EntityMetaData expectedMetadata = EntityMetaDataImpl.newInstance(hopMetaData);
	//		expectedMetadata.setName(entityName);
	//		expectedMetadata.addAttribute("source");
	//		assertEquals(actual.getEntityMetaData(), expectedMetadata);
	//		Set<Entity> created = Sets.newHashSet(actual.iterator());
	//
	//		MapEntity expected1 = new MapEntity(expectedMetadata);
	//		expected1.set("identifier", "1");
	//		expected1.set("height", new Double(2));
	//		expected1.set("source", "Gene");
	//		MapEntity expected2 = new MapEntity(expectedMetadata);
	//		expected2.set("identifier", "2");
	//		expected2.set("height", new Double(4));
	//		expected2.set("source", "Gene");
	//		MapEntity expected3 = new MapEntity(expectedMetadata);
	//		expected3.set("identifier", "3");
	//		expected3.set("height", new Double(6));
	//		expected3.set("source", "Gene");
	//		MapEntity expected4 = new MapEntity(expectedMetadata);
	//		expected4.set("identifier", "4");
	//		expected4.set("height", new Double(8));
	//		expected4.set("source", "Gene");
	//
	//		assertEquals(created, ImmutableSet.<Entity> of(expected1, expected2, expected3, expected4));
	//	}
	//
	//	/**
	//	 * Applying a mapping multiple times to the same target should update the existing entities.
	//	 */
	//	@Test
	//	public void testUpdate()
	//	{
	//		String entityName = "updateEntity";
	//		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());
	//
	//		// make project and apply mappings once
	//		MappingProject project = createMappingProjectWithMappings(entityName);
	//
	//		// update an entity in the source
	//		Entity geneEntity = dataService.findOneById(geneMetaData.getName(), "2");
	//		geneEntity.set("length", 5.678);
	//		dataService.update(geneMetaData.getName(), geneEntity);
	//
	//		// apply mapping again
	//		mappingService.applyMappings(project.getMappingTarget("HopEntity"), entityName);
	//
	//		Repository<Entity> actual = dataService.getRepository(entityName);
	//		EntityMetaData expectedMetadata = EntityMetaDataImpl.newInstance(hopMetaData);
	//		expectedMetadata.setName(entityName);
	//		expectedMetadata.addAttribute("source");
	//		assertEquals(actual.getEntityMetaData(), expectedMetadata);
	//		Set<Entity> created = Sets.newHashSet(actual.iterator());
	//
	//		MapEntity expected1 = new MapEntity(expectedMetadata);
	//		expected1.set("identifier", "1");
	//		expected1.set("height", new Double(2));
	//		expected1.set("source", "Gene");
	//		MapEntity expected2 = new MapEntity(expectedMetadata);
	//		expected2.set("identifier", "2");
	//		expected2.set("height", 5.678);
	//		expected2.set("source", "Gene");
	//		MapEntity expected3 = new MapEntity(expectedMetadata);
	//		expected3.set("identifier", "3");
	//		expected3.set("height", new Double(6));
	//		expected3.set("source", "Gene");
	//
	//		assertEquals(created, ImmutableSet.<Entity> of(expected1, expected2, expected3));
	//	}
	//
	//	/**
	//	 * Removing an entity in the source and applying the mapping again should also delete this entity in the target.
	//	 */
	//	@Test
	//	public void testDelete()
	//	{
	//		String entityName = "deleteEntity";
	//		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());
	//
	//		// make project and apply mappings once
	//		MappingProject project = createMappingProjectWithTwoSourcesWithMappings(entityName);
	//
	//		// delete an entity from the source
	//		dataService.deleteById(geneMetaData.getName(), "2");
	//
	//		// apply mapping again, this should not delete entities mapped from source 2
	//		mappingService.applyMappings(project.getMappingTarget("HopEntity"), entityName);
	//
	//		Repository<Entity> actual = dataService.getRepository(entityName);
	//		EntityMetaData expectedMetadata = EntityMetaDataImpl.newInstance(hopMetaData);
	//		expectedMetadata.setName(entityName);
	//		expectedMetadata.addAttribute("source");
	//		assertEquals(actual.getEntityMetaData(), expectedMetadata);
	//		Set<Entity> created = Sets.newHashSet(actual.iterator());
	//
	//		MapEntity expected1 = new MapEntity(expectedMetadata);
	//		expected1.set("identifier", "1");
	//		expected1.set("height", new Double(2));
	//		expected1.set("source", "Gene");
	//		MapEntity expected3 = new MapEntity(expectedMetadata);
	//		expected3.set("identifier", "3");
	//		expected3.set("height", new Double(6));
	//		expected3.set("source", "Gene");
	//		MapEntity expected4 = new MapEntity(expectedMetadata);
	//		expected4.set("identifier", "A");
	//		expected4.set("height", new Double(12345));
	//		expected4.set("source", "Exon");
	//
	//		assertEquals(created, ImmutableSet.<Entity> of(expected1, expected3, expected4));
	//	}
	//
	//	@Test(expectedExceptions = MolgenisDataException.class)
	//	public void testTargetMetaNotCompatible()
	//	{
	//		MappingProject project = createMappingProjectWithMappings("compatibleEntity");
	//		MappingTarget target = project.getMappingTarget("HopEntity");
	//
	//		// apply mapping to the wrong target
	//		mappingService.applyMappings(target, geneMetaData.getName());
	//	}
	//
	//	private MappingProject createMappingProjectWithMappings(String newEntityName)
	//	{
	//		MappingProject mappingProject = mappingService.addMappingProject("TestRun", user, "HopEntity");
	//		MappingTarget target = mappingProject.getMappingTarget("HopEntity");
	//		EntityMapping mapping = target.addSource(geneMetaData);
	//
	//		AttributeMapping idMapping = mapping.addAttributeMapping("identifier");
	//		idMapping.setAlgorithm("$('id').value()");
	//		AttributeMapping attrMapping = mapping.addAttributeMapping("height");
	//		attrMapping.setAlgorithm("$('length').value()");
	//
	//		mappingService.applyMappings(target, newEntityName);
	//		return mappingProject;
	//	}
	//
	//	private MappingProject createMappingProjectWithTwoSourcesWithMappings(String newEntityName)
	//	{
	//		MappingProject mappingProject = mappingService.addMappingProject("TestRun", user, "HopEntity");
	//		MappingTarget target = mappingProject.getMappingTarget("HopEntity");
	//		EntityMapping mapping = target.addSource(geneMetaData);
	//
	//		AttributeMapping idMapping = mapping.addAttributeMapping("identifier");
	//		idMapping.setAlgorithm("$('id').value()");
	//		AttributeMapping attrMapping = mapping.addAttributeMapping("height");
	//		attrMapping.setAlgorithm("$('length').value()");
	//
	//		EntityMapping mapping2 = target.addSource(exonMetaData);
	//		AttributeMapping idMapping2 = mapping2.addAttributeMapping("identifier");
	//		idMapping2.setAlgorithm("$('id').value()");
	//		AttributeMapping attrMapping2 = mapping2.addAttributeMapping("height");
	//		attrMapping2.setAlgorithm("$('basepairs').value()");
	//
	//		mappingService.applyMappings(target, newEntityName);
	//		return mappingProject;
	//	}
	//
	//	@Test
	//	public void testNumericId()
	//	{
	//		assertEquals(mappingService.generateId(MolgenisFieldTypes.INT, 1l), "2");
	//		assertEquals(mappingService.generateId(MolgenisFieldTypes.DECIMAL, 2l), "3");
	//		assertEquals(mappingService.generateId(MolgenisFieldTypes.LONG, 3l), "4");
	//	}
	//
	//	@Test
	//	public void testStringId()
	//	{
	//		reset(idGenerator);
	//		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());
	//
	//		mappingService.generateId(MolgenisFieldTypes.STRING, 1l);
	//		verify(idGenerator).generateId();
	//	}
	//
	//	@Configuration
	//	static class Config
	//	{
	//		@Bean
	//		DataServiceImpl dataService()
	//		{
	//			return new DataServiceImpl();
	//		}
	//
	//		@Bean
	//		PlatformTransactionManager platformTransactionManager()
	//		{
	//			return mock(PlatformTransactionManager.class);
	//		}
	//
	//		@Bean
	//		MetaDataService metaDataService()
	//		{
	//			return new MetaDataServiceImpl(dataService());
	//		}
	//
	//		@Bean
	//		MolgenisUserService userService()
	//		{
	//			return mock(MolgenisUserService.class);
	//		}
	//
	//		@Bean
	//		RepositoryCollection RepositoryCollection()
	//		{
	//			return new InMemoryRepositoryCollection("mem");
	//		}
	//
	//		@Bean
	//		PermissionSystemService permissionSystemService()
	//		{
	//			return mock(PermissionSystemService.class);
	//		}
	//
	//		@Bean
	//		SemanticSearchService semanticSearchService()
	//		{
	//			return mock(SemanticSearchService.class);
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
	//
	//		@Bean
	//		public LanguageService languageService()
	//		{
	//			return new LanguageService(dataService(), new AppDbSettings());
	//		}
	//
	//		@Bean
	//		public FreeMarkerConfigurer freeMarkerConfigurer()
	//		{
	//			return new FreeMarkerConfigurer();
	//		}
	//
	//		@PostConstruct
	//		public void initRepositories()
	//		{
	//			MetaDataService metaDataService = metaDataService();
	//			dataService().setMetaDataService(metaDataService);
	//
	//			RepositoryCollection RepositoryCollection = RepositoryCollection();
	//			metaDataService.setDefaultBackend(RepositoryCollection);
	//			// FIXME
	////			metaDataService.addEntityMeta(new MolgenisUserMetaData());
	////			metaDataService.addEntityMeta(AttributeMappingRepositoryImpl.META_DATA);
	////			metaDataService.addEntityMeta(EntityMappingRepositoryImpl.META_DATA);
	////			metaDataService.addEntityMeta(MappingTargetRepositoryImpl.META_DATA);
	////			metaDataService.addEntityMeta(MappingProjectRepositoryImpl.META_DATA);
	//		}
	//	}
}
