package org.molgenis.data.mapper.service.impl;

import com.google.common.collect.Sets;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.MolgenisUserFactory;
import org.molgenis.data.*;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.mapper.config.MappingConfig;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.mapping.model.MappingProject;
import org.molgenis.data.mapper.mapping.model.MappingTarget;
import org.molgenis.data.mapper.meta.MappingTargetMetaData;
import org.molgenis.data.mapper.repository.MappingProjectRepository;
import org.molgenis.data.mapper.service.AlgorithmService;
import org.molgenis.data.mapper.service.MappingService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.reindex.ReindexActionRegisterService;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.of;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.mapper.meta.MappingProjectMetaData.*;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { MappingServiceImplTest.Config.class, MappingConfig.class })
public class MappingServiceImplTest extends AbstractMolgenisSpringTest
{
	public static final String HOP_ENTITY = "HopEntity";
	public static final String USERNAME = "admin";
	public static final String GENE_ENTITY = "Gene";
	public static final String EXON_ENTITY = "Exon";

	@Autowired
	private EntityMetaDataFactory entityMetaFactory;

	@Autowired
	private AttributeMetaDataFactory attrMetaFactory;

	@Autowired
	private MolgenisUserFactory molgenisUserFactory;

	@Autowired
	private DataService dataService;

	@Autowired
	private MetaDataService metaDataService;

	@Autowired
	private MappingService mappingService;

	@Autowired
	private MolgenisUserService userService;

	@Autowired
	private PermissionSystemService permissionSystemService;

	@Autowired
	private IdGenerator idGenerator;

	@Autowired
	private MappingProjectRepository mappingProjectRepository;

	private MolgenisUser user;
	private EntityMetaData hopMetaData;
	private EntityMetaData geneMetaData;
	private EntityMetaData exonMetaData;

	private final UuidGenerator uuidGenerator = new UuidGenerator();

	@BeforeMethod
	public void beforeMethod()
	{
		reset(dataService);
		reset(metaDataService);
		when(dataService.getMeta()).thenReturn(metaDataService);

		user = molgenisUserFactory.create();
		user.setUsername(USERNAME);
		when(userService.getUser(USERNAME)).thenReturn(user);

		Package package_ = mock(Package.class);

		hopMetaData = entityMetaFactory.create(HOP_ENTITY).setPackage(package_);
		hopMetaData.addAttribute(attrMetaFactory.create().setName("identifier"), ROLE_ID);
		hopMetaData.addAttribute(attrMetaFactory.create().setName("height").setDataType(DECIMAL).setNillable(false));

		geneMetaData = entityMetaFactory.create(GENE_ENTITY).setPackage(package_);
		geneMetaData.addAttribute(attrMetaFactory.create().setName("id"), ROLE_ID);
		geneMetaData.addAttribute(attrMetaFactory.create().setName("length").setDataType(DECIMAL).setNillable(false));

		exonMetaData = entityMetaFactory.create(EXON_ENTITY).setPackage(package_);
		exonMetaData.addAttribute(attrMetaFactory.create().setName("id"), ROLE_ID);
		exonMetaData
				.addAttribute(attrMetaFactory.create().setName("basepairs").setDataType(DECIMAL).setNillable(false));

		when(dataService.getEntityMetaData(HOP_ENTITY)).thenReturn(hopMetaData);
		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());
	}

	@Test
	public void testAddMappingProject()
	{
		String projectName = "test_project";

		// Create a mappingProject via the mappingService
		MappingProject actualAddedMappingProject = getMappingServiceMappingProject(projectName);

		// Make sure the identifiers of both the project and the target are not null
		final String mappingProjectId = actualAddedMappingProject.getIdentifier();
		assertNotNull(mappingProjectId);

		final String mappingTargetId = actualAddedMappingProject.getMappingTarget(HOP_ENTITY).getIdentifier();
		assertNotNull(mappingTargetId);

		// Create a mappingTarget which should be equal to the mapping project created by the mappingService
		MappingTarget expectedMappingTarget = getManualMappingTarget(mappingTargetId, emptyList());

		// Check if the mapping project is created properly
		assertEquals(actualAddedMappingProject.getName(), projectName);
		assertEquals(actualAddedMappingProject.getOwner(), user);
		assertEquals(actualAddedMappingProject.getMappingTarget(HOP_ENTITY), expectedMappingTarget);

		// Create a mapping project which should be equal to the mapping project created by the mappingService
		MappingProject expectedAddedMappingProject = getManualMappingProject(mappingProjectId, projectName,
				expectedMappingTarget);

		// Assert the actual and the expected project are equal
		assertEquals(actualAddedMappingProject, expectedAddedMappingProject);
	}

	//@Test
	public void testGetMappingProject()
	{
		String projectName = "test_project";

		MappingProject actualAddedMappingProject = mappingService.addMappingProject(projectName, user, HOP_ENTITY);

		String mappingTargetIdentifier = actualAddedMappingProject.getMappingTarget(HOP_ENTITY).getIdentifier();
		MappingTarget expectedMappingTarget = getManualMappingTarget(mappingTargetIdentifier, emptyList());

		String mappingProjectIdentifier = actualAddedMappingProject.getIdentifier();
		MappingProject expectedMappingProject = getManualMappingProject(mappingProjectIdentifier, projectName,
				expectedMappingTarget);

		Entity mappingTargetEntity = mock(Entity.class);
		when(mappingTargetEntity.getString(MappingTargetMetaData.IDENTIFIER)).thenReturn(mappingTargetIdentifier);
		when(mappingTargetEntity.getString(MappingTargetMetaData.TARGET)).thenReturn(HOP_ENTITY);
		when(mappingTargetEntity.getEntities(MappingTargetMetaData.ENTITY_MAPPINGS)).thenReturn(null);
		when(dataService.getEntityMetaData(mappingTargetEntity.getString(MappingTargetMetaData.TARGET)))
				.thenReturn(hopMetaData);

		Entity mappingProjectEntity = mock(Entity.class);
		when(mappingProjectEntity.get(IDENTIFIER)).thenReturn(mappingProjectIdentifier);
		when(mappingProjectEntity.get(NAME)).thenReturn(projectName);
		when(mappingProjectEntity.get(OWNER)).thenReturn(user);
		when(mappingProjectEntity.getEntities(MAPPING_TARGETS)).thenReturn(singletonList(mappingTargetEntity));
		when(mappingProjectEntity.get(MAPPING_TARGETS)).thenReturn(singletonList(expectedMappingTarget));

		when(dataService.findOneById(MAPPING_PROJECT, mappingProjectIdentifier)).thenReturn(mappingProjectEntity);
		when(mappingProjectRepository.getMappingProject(mappingProjectIdentifier))
				.thenReturn(actualAddedMappingProject);
		MappingProject retrievedMappingProject = mappingService.getMappingProject(mappingProjectIdentifier);

		assertEquals(retrievedMappingProject, expectedMappingProject);

		// mappingService.applyMappings()
		// mappingService.cloneMappingProject()
		// mappingService.deleteMappingProject();
		// mappingService.generateId()
		// mappingService.getAllMappingProjects()
		// mappingService.updateMappingProject();

		// Test adding new source
		// Test adding new target
		// Test adding existing target

	}

	//@Test
	// TODO add unit test for testCloneMappingProject when InMemoryRepositoryCollection supports Query.
	public void testCloneMappingProjectString()
	{
		when(idGenerator.generateId()).thenReturn("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");

		MappingProject mappingProject = createMappingProjectWithMappings("testCloneMappingProject");
		mappingService.updateMappingProject(mappingProject);

		MappingProject clonedMappingProject = mappingService
				.cloneMappingProject(mappingProject.getIdentifier(), "Clone of TestRun");

		List<MappingTarget> mappingTargets = mappingProject.getMappingTargets();
		List<MappingTarget> clonedMappingTargets = clonedMappingProject.getMappingTargets();
		assertEquals(mappingTargets.size(), clonedMappingTargets.size());
		for (int i = 0; i < mappingTargets.size(); ++i)
		{
			MappingTarget mappingTarget = mappingTargets.get(i);
			MappingTarget clonedMappingTarget = clonedMappingTargets.get(i);

			assertNotEquals(mappingTarget.getIdentifier(), clonedMappingTarget.getIdentifier());
			assertEquals(mappingTarget.getTarget().getName(), clonedMappingTarget.getTarget().getName());

			List<EntityMapping> entityMappings = newArrayList(mappingTarget.getEntityMappings());
			List<EntityMapping> clonedEntityMappings = newArrayList(clonedMappingTarget.getEntityMappings());
			assertEquals(entityMappings.size(), clonedEntityMappings.size());

			for (int j = 0; j < entityMappings.size(); ++j)
			{
				EntityMapping entityMapping = entityMappings.get(j);
				EntityMapping clonedEntityMapping = clonedEntityMappings.get(j);

				assertNotEquals(entityMapping.getIdentifier(), clonedEntityMapping.getIdentifier());
				assertEquals(entityMapping.getLabel(), clonedEntityMapping.getLabel());
				assertEquals(entityMapping.getName(), clonedEntityMapping.getName());
				assertEquals(entityMapping.getSourceEntityMetaData().getName(),
						clonedEntityMapping.getSourceEntityMetaData().getName());
				assertEquals(entityMapping.getTargetEntityMetaData().getName(),
						clonedEntityMapping.getTargetEntityMetaData().getName());

				List<AttributeMapping> attributeMappings = newArrayList(entityMapping.getAttributeMappings());
				List<AttributeMapping> clonedAttributeMappings = newArrayList(
						clonedEntityMapping.getAttributeMappings());
				assertEquals(attributeMappings.size(), clonedAttributeMappings.size());

				for (int k = 0; k < attributeMappings.size(); ++k)
				{
					AttributeMapping attributeMapping = attributeMappings.get(k);
					AttributeMapping clonedAttributeMapping = clonedAttributeMappings.get(k);
					assertNotEquals(attributeMapping.getIdentifier(), clonedAttributeMapping.getIdentifier());

					assertEquals(attributeMapping.getAlgorithm(), clonedAttributeMapping.getAlgorithm());
					assertEquals(attributeMapping.getTargetAttributeMetaData().getName(),
							clonedAttributeMapping.getTargetAttributeMetaData().getName());
				}
			}
		}
	}

	//@Test
	public void testAddTarget()
	{
		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());

		MappingProject mappingProject = mappingService.addMappingProject("Test123", user, HOP_ENTITY);
		mappingProject.addTarget(geneMetaData);

		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());
		mappingService.updateMappingProject(mappingProject);

		MappingProject retrieved = mappingService.getMappingProject(mappingProject.getIdentifier());
		assertEquals(mappingProject, retrieved);
	}

	//@Test(expectedExceptions = IllegalStateException.class)
	public void testAddExistingTarget()
	{
		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());

		MappingProject mappingProject = mappingService.addMappingProject("Test123", user, HOP_ENTITY);
		mappingProject.addTarget(hopMetaData);
	}

	//@Test
	public void testAddNewSource()
	{
		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());

		MappingProject mappingProject = mappingService.addMappingProject("Test123", user, HOP_ENTITY);

		// now add new source
		EntityMapping mapping = mappingProject.getMappingTarget(HOP_ENTITY).addSource(geneMetaData);
		mappingService.updateMappingProject(mappingProject);

		MappingProject retrieved = mappingService.getMappingProject(mappingProject.getIdentifier());
		assertEquals(retrieved, mappingProject);

		assertEquals(retrieved.getMappingTarget(HOP_ENTITY).getMappingForSource(GENE_ENTITY), mapping);
	}

	//@Test
	public void testAddExistingSource()
	{
		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());

		MappingProject mappingProject = mappingService.addMappingProject("Test123", user, HOP_ENTITY);
		mappingProject.getMappingTarget(HOP_ENTITY).addSource(geneMetaData);

		mappingService.updateMappingProject(mappingProject);
		MappingProject retrieved = mappingService.getMappingProject(mappingProject.getIdentifier());
		try
		{
			retrieved.getMappingTarget(HOP_ENTITY).addSource(geneMetaData);
			fail("Expected exception");
		}
		catch (IllegalStateException ignored)
		{
		}
	}

	//@Test
	public void testApplyMappings()
	{
		String entityName = "Koetjeboe";
		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());
		createMappingProjectWithMappings(entityName);

		Repository<Entity> actual = dataService.getRepository(entityName);
		EntityMetaData expectedMetadata = EntityMetaData.newInstance(hopMetaData);
		expectedMetadata.addAttribute(attrMetaFactory.create().setName("source"));
		assertEquals(actual.getEntityMetaData(), expectedMetadata);
		Set<Entity> created = Sets.newHashSet(actual.iterator());

		Entity koetje1 = new DynamicEntity(expectedMetadata);
		koetje1.set("identifier", "1");
		koetje1.set("height", 2d);
		koetje1.set("source", GENE_ENTITY);
		Entity koetje2 = new DynamicEntity(expectedMetadata);
		koetje2.set("identifier", "2");
		koetje2.set("height", 4d);
		koetje2.set("source", GENE_ENTITY);
		Entity koetje3 = new DynamicEntity(expectedMetadata);
		koetje3.set("identifier", "3");
		koetje3.set("height", 6d);
		koetje3.set("source", GENE_ENTITY);

		assertEquals(created, of(koetje1, koetje2, koetje3));
		verify(permissionSystemService)
				.giveUserEntityPermissions(SecurityContextHolder.getContext(), singletonList(entityName));
	}

	/**
	 * New entities in the source should be added to the target when a new mapping to the same target is performed.
	 */
	//@Test
	public void testAdd()
	{
		String entityName = "addEntity";
		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());

		// make project and apply mappings once
		MappingProject project = createMappingProjectWithMappings(entityName);

		// add an entity to the source
		Entity geneEntity = new DynamicEntity(geneMetaData);
		geneEntity.set("id", "4");
		geneEntity.set("length", 8d);
		dataService.add(geneMetaData.getName(), geneEntity);

		// apply mapping again
		mappingService.applyMappings(project.getMappingTarget(HOP_ENTITY), entityName);

		Repository<Entity> actual = dataService.getRepository(entityName);
		EntityMetaData expectedMetadata = EntityMetaData.newInstance(hopMetaData);
		expectedMetadata.addAttribute(attrMetaFactory.create().setName("source"));
		assertEquals(actual.getEntityMetaData(), expectedMetadata);
		Set<Entity> created = Sets.newHashSet(actual.iterator());

		Entity expected1 = new DynamicEntity(expectedMetadata);
		expected1.set("identifier", "1");
		expected1.set("height", 2d);
		expected1.set("source", GENE_ENTITY);
		Entity expected2 = new DynamicEntity(expectedMetadata);
		expected2.set("identifier", "2");
		expected2.set("height", 4d);
		expected2.set("source", GENE_ENTITY);
		Entity expected3 = new DynamicEntity(expectedMetadata);
		expected3.set("identifier", "3");
		expected3.set("height", 6d);
		expected3.set("source", GENE_ENTITY);
		Entity expected4 = new DynamicEntity(expectedMetadata);
		expected4.set("identifier", "4");
		expected4.set("height", 8d);
		expected4.set("source", GENE_ENTITY);

		assertEquals(created, of(expected1, expected2, expected3, expected4));
	}

	/**
	 * Applying a mapping multiple times to the same target should update the existing entities.
	 */
	//@Test
	public void testUpdate()
	{
		String entityName = "updateEntity";
		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());

		// make project and apply mappings once
		MappingProject project = createMappingProjectWithMappings(entityName);

		// update an entity in the source
		Entity geneEntity = dataService.findOneById(geneMetaData.getName(), "2");
		geneEntity.set("length", 5.678);
		dataService.update(geneMetaData.getName(), geneEntity);

		// apply mapping again
		mappingService.applyMappings(project.getMappingTarget(HOP_ENTITY), entityName);

		Repository<Entity> actual = dataService.getRepository(entityName);
		EntityMetaData expectedMetadata = EntityMetaData.newInstance(hopMetaData);
		expectedMetadata.addAttribute(attrMetaFactory.create().setName("source"));
		assertEquals(actual.getEntityMetaData(), expectedMetadata);
		Set<Entity> created = Sets.newHashSet(actual.iterator());

		Entity expected1 = new DynamicEntity(expectedMetadata);
		expected1.set("identifier", "1");
		expected1.set("height", 2d);
		expected1.set("source", GENE_ENTITY);
		Entity expected2 = new DynamicEntity(expectedMetadata);
		expected2.set("identifier", "2");
		expected2.set("height", 5.678);
		expected2.set("source", GENE_ENTITY);
		Entity expected3 = new DynamicEntity(expectedMetadata);
		expected3.set("identifier", "3");
		expected3.set("height", 6d);
		expected3.set("source", GENE_ENTITY);

		assertEquals(created, of(expected1, expected2, expected3));
	}

	/**
	 * Removing an entity in the source and applying the mapping again should also delete this entity in the target.
	 */
	//@Test
	public void testDelete()
	{
		String entityName = "deleteEntity";
		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());

		// make project and apply mappings once
		MappingProject project = createMappingProjectWithTwoSourcesWithMappings(entityName);

		// delete an entity from the source
		dataService.deleteById(geneMetaData.getName(), "2");

		// apply mapping again, this should not delete entities mapped from source 2
		mappingService.applyMappings(project.getMappingTarget(HOP_ENTITY), entityName);

		Repository<Entity> actual = dataService.getRepository(entityName);
		EntityMetaData expectedMetadata = EntityMetaData.newInstance(hopMetaData);
		expectedMetadata.addAttribute(attrMetaFactory.create().setName("source"));
		assertEquals(actual.getEntityMetaData(), expectedMetadata);
		Set<Entity> created = Sets.newHashSet(actual.iterator());

		Entity expected1 = new DynamicEntity(expectedMetadata);
		expected1.set("identifier", "1");
		expected1.set("height", 2d);
		expected1.set("source", GENE_ENTITY);
		Entity expected3 = new DynamicEntity(expectedMetadata);
		expected3.set("identifier", "3");
		expected3.set("height", 6d);
		expected3.set("source", GENE_ENTITY);
		Entity expected4 = new DynamicEntity(expectedMetadata);
		expected4.set("identifier", "A");
		expected4.set("height", 12345d);
		expected4.set("source", EXON_ENTITY);

		assertEquals(created, of(expected1, expected3, expected4));
	}

	//@Test(expectedExceptions = MolgenisDataException.class)
	public void testTargetMetaNotCompatible()
	{
		MappingProject project = createMappingProjectWithMappings("compatibleEntity");
		MappingTarget target = project.getMappingTarget(HOP_ENTITY);

		// apply mapping to the wrong target
		mappingService.applyMappings(target, geneMetaData.getName());
	}

	private MappingProject createMappingProjectWithMappings(String newEntityName)
	{
		String mappingProjectName = "TestRun";
		String targetEntity = HOP_ENTITY;

		MappingProject mockMappingProject = new MappingProject(mappingProjectName, user);
		mockMappingProject.addTarget(dataService.getEntityMetaData(targetEntity));

		when(mappingService.addMappingProject(mappingProjectName, user, targetEntity)).thenReturn(mockMappingProject);
		MappingProject mappingProject = mappingService.addMappingProject(mappingProjectName, user, targetEntity);
		MappingTarget target = mappingProject.getMappingTarget(targetEntity);
		EntityMapping mapping = target.addSource(geneMetaData);

		AttributeMapping idMapping = mapping.addAttributeMapping("identifier");
		idMapping.setAlgorithm("$('id').value()");
		AttributeMapping attrMapping = mapping.addAttributeMapping("height");
		attrMapping.setAlgorithm("$('length').value()");

		mappingService.applyMappings(target, newEntityName);
		return mappingProject;
	}

	private MappingProject createMappingProjectWithTwoSourcesWithMappings(String newEntityName)
	{
		MappingProject mappingProject = mappingService.addMappingProject("TestRun", user, HOP_ENTITY);
		MappingTarget target = mappingProject.getMappingTarget(HOP_ENTITY);
		EntityMapping mapping = target.addSource(geneMetaData);

		AttributeMapping idMapping = mapping.addAttributeMapping("identifier");
		idMapping.setAlgorithm("$('id').value()");
		AttributeMapping attrMapping = mapping.addAttributeMapping("height");
		attrMapping.setAlgorithm("$('length').value()");

		EntityMapping mapping2 = target.addSource(exonMetaData);
		AttributeMapping idMapping2 = mapping2.addAttributeMapping("identifier");
		idMapping2.setAlgorithm("$('id').value()");
		AttributeMapping attrMapping2 = mapping2.addAttributeMapping("height");
		attrMapping2.setAlgorithm("$('basepairs').value()");

		mappingService.applyMappings(target, newEntityName);
		return mappingProject;
	}

	//@Test
	public void testNumericId()
	{
		assertEquals(mappingService.generateId(INT, 1L), "2");
		assertEquals(mappingService.generateId(DECIMAL, 2L), "3");
		assertEquals(mappingService.generateId(LONG, 3L), "4");
	}

	//@Test
	public void testStringId()
	{
		reset(idGenerator);
		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());

		mappingService.generateId(STRING, 1L);
		verify(idGenerator).generateId();
	}

	private MappingProject getMappingServiceMappingProject(String projectName)
	{
		return mappingService.addMappingProject(projectName, user, HOP_ENTITY);
	}

	private MappingTarget getManualMappingTarget(String identifier, Collection<EntityMapping> entityMappings)
	{
		return new MappingTarget(identifier, hopMetaData, entityMappings);
	}

	private MappingProject getManualMappingProject(String identifier, String projectName, MappingTarget mappingTarget)
	{
		return new MappingProject(identifier, projectName, user, newArrayList(mappingTarget));
	}

	@Configuration
	@ComponentScan({ "org.molgenis.data.mapper.meta", "org.molgenis.auth", "org.molgenis.data.reindex.meta" })
	static class Config
	{
		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public MetaDataService metaDataService()
		{
			return mock(MetaDataService.class);
		}

		@Bean
		public MolgenisUserService userService()
		{
			return mock(MolgenisUserService.class);
		}

		@Bean
		public PermissionSystemService permissionSystemService()
		{
			return mock(PermissionSystemService.class);
		}

		@Bean
		public SemanticSearchService semanticSearchService()
		{
			return mock(SemanticSearchService.class);
		}

		@Bean
		public IdGenerator idGenerator()
		{
			return mock(IdGenerator.class);
		}

		@Bean
		public OntologyTagService ontologyTagService()
		{
			return mock(OntologyTagService.class);
		}

		@Bean
		public LanguageService languageService()
		{
			return mock(LanguageService.class);
		}

		@Bean
		public ReindexActionRegisterService reindexActionRegisterService()
		{
			return mock(ReindexActionRegisterService.class);
		}

		@Bean
		public FreeMarkerConfigurer freeMarkerConfigurer()
		{
			return mock(FreeMarkerConfigurer.class);
		}

		@Bean
		public AlgorithmService algorithmService()
		{
			return mock(AlgorithmService.class);
		}

		@Bean
		public MappingProjectRepository mappingProjectRepository()
		{
			return mock(MappingProjectRepository.class);
		}

		@Bean
		public AttributeMetaDataFactory attributeMetaDataFactory()
		{
			return new AttributeMetaDataFactory();
		}

		@Bean
		public MappingService mappingService()
		{
			return new MappingServiceImpl(dataService(), algorithmService(), idGenerator(), mappingProjectRepository(),
					permissionSystemService(), attributeMetaDataFactory());
		}
	}
}
