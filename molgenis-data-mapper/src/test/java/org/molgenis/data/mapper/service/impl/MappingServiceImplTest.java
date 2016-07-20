package org.molgenis.data.mapper.service.impl;

import com.google.common.collect.Lists;
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
import org.molgenis.data.mapper.service.MappingService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemEntityMetaDataRegistry;
import org.molgenis.data.reindex.ReindexActionRegisterService;
import org.molgenis.data.reindex.ReindexActionRegisterServiceImpl;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.molgenis.ui.settings.AppDbSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.of;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { MappingServiceImplTest.Config.class, MappingConfig.class })
public class MappingServiceImplTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityMetaDataFactory entityMetaFactory;

	@Autowired
	private AttributeMetaDataFactory attrMetaFactory;

	@Autowired
	private MolgenisUserFactory molgenisUserFactory;

	@Autowired
	private DataServiceImpl dataService;

	@Autowired
	private MappingService mappingService;

	@Autowired
	private MolgenisUserService userService;

	@Autowired
	private PermissionSystemService permissionSystemService;

	@Autowired
	private IdGenerator idGenerator;

	private MolgenisUser user;

	private EntityMetaData hopMetaData;

	private EntityMetaData geneMetaData;

	private EntityMetaData exonMetaData;

	private final UuidGenerator uuidGenerator = new UuidGenerator();

	@BeforeMethod
	public void beforeMethod()
	{
		user = molgenisUserFactory.create();
		user.setUsername("Piet");
		when(userService.getUser("Piet")).thenReturn(user);

		Package package_ = mock(Package.class);

		hopMetaData = entityMetaFactory.create("HopEntity").setPackage(package_);
		hopMetaData.addAttribute(attrMetaFactory.create().setName("identifier"), ROLE_ID);
		hopMetaData.addAttribute(attrMetaFactory.create().setName("height").setDataType(DECIMAL).setNillable(false));

		geneMetaData = entityMetaFactory.create("Gene").setPackage(package_);
		geneMetaData.addAttribute(attrMetaFactory.create().setName("id"), ROLE_ID);
		geneMetaData.addAttribute(attrMetaFactory.create().setName("length").setDataType(DECIMAL).setNillable(false));

		exonMetaData = entityMetaFactory.create("Exon").setPackage(package_);
		exonMetaData.addAttribute(attrMetaFactory.create().setName("id"), ROLE_ID);
		exonMetaData
				.addAttribute(attrMetaFactory.create().setName("basepairs").setDataType(DECIMAL).setNillable(false));

		when(dataService.getEntityMetaData("HopEntity")).thenReturn(hopMetaData);

		// add 3 Gene entities
		Repository<Entity> gene = dataService.getMeta().addEntityMeta(geneMetaData);
		gene.deleteAll(); // refresh
		for (int i = 1; i < 4; i++)
		{
			Entity geneEntity = new DynamicEntity(geneMetaData);
			geneEntity.set("id", Integer.valueOf(i).toString());
			geneEntity.set("length", i * 2);
			gene.add(geneEntity);
		}

		// add 1 Exon entity
		Repository<Entity> exon = dataService.getMeta().addEntityMeta(exonMetaData);
		exon.deleteAll(); // refresh
		Entity geneEntity = new DynamicEntity(exonMetaData);
		geneEntity.set("id", "A");
		geneEntity.set("basepairs", 12345d);
		exon.add(geneEntity);

		dataService.getEntityNames().forEach(entityName -> dataService.getMeta().deleteEntityMeta(entityName));

		TestingAuthenticationToken authentication = new TestingAuthenticationToken("userName", null);
		authentication.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);

	}

	@Test
	public void testAddMappingProject()
	{
		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());

		MappingProject added = mappingService.addMappingProject("Test123", user, "HopEntity");
		assertEquals(added.getName(), "Test123");

		MappingProject expected = new MappingProject("Test123", user);
		expected.addTarget(hopMetaData);

		final String mappingProjectId = added.getIdentifier();
		assertNotNull(mappingProjectId);
		expected.setIdentifier(mappingProjectId);

		final String mappingTargetId = added.getMappingTarget("HopEntity").getIdentifier();
		assertNotNull(mappingTargetId);
		expected.getMappingTarget("HopEntity").setIdentifier(mappingTargetId);
		assertEquals(added, expected);

		MappingProject retrieved = mappingService.getMappingProject(mappingProjectId);
		assertEquals(retrieved, expected);
	}

	// TODO add unit test for testCloneMappingProject when InMemoryRepositoryCollection supports Query.
	@Test
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

			List<EntityMapping> entityMappings = Lists.newArrayList(mappingTarget.getEntityMappings());
			List<EntityMapping> clonedEntityMappings = Lists.newArrayList(clonedMappingTarget.getEntityMappings());
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

				List<AttributeMapping> attributeMappings = Lists.newArrayList(entityMapping.getAttributeMappings());
				List<AttributeMapping> clonedAttributeMappings = Lists
						.newArrayList(clonedEntityMapping.getAttributeMappings());
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

	@Test
	public void testAddTarget()
	{
		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());

		MappingProject mappingProject = mappingService.addMappingProject("Test123", user, "HopEntity");
		mappingProject.addTarget(geneMetaData);

		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());
		mappingService.updateMappingProject(mappingProject);

		MappingProject retrieved = mappingService.getMappingProject(mappingProject.getIdentifier());
		assertEquals(mappingProject, retrieved);
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testAddExistingTarget()
	{
		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());

		MappingProject mappingProject = mappingService.addMappingProject("Test123", user, "HopEntity");
		mappingProject.addTarget(hopMetaData);
	}

	@Test
	public void testAddNewSource()
	{
		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());

		MappingProject mappingProject = mappingService.addMappingProject("Test123", user, "HopEntity");

		// now add new source
		EntityMapping mapping = mappingProject.getMappingTarget("HopEntity").addSource(geneMetaData);
		mappingService.updateMappingProject(mappingProject);

		MappingProject retrieved = mappingService.getMappingProject(mappingProject.getIdentifier());
		assertEquals(retrieved, mappingProject);

		assertEquals(retrieved.getMappingTarget("HopEntity").getMappingForSource("Gene"), mapping);
	}

	@Test
	public void testAddExistingSource()
	{
		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());

		MappingProject mappingProject = mappingService.addMappingProject("Test123", user, "HopEntity");
		mappingProject.getMappingTarget("HopEntity").addSource(geneMetaData);

		mappingService.updateMappingProject(mappingProject);
		MappingProject retrieved = mappingService.getMappingProject(mappingProject.getIdentifier());
		try
		{
			retrieved.getMappingTarget("HopEntity").addSource(geneMetaData);
			fail("Expected exception");
		}
		catch (IllegalStateException ignored)
		{
		}
	}

	@Test
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
		koetje1.set("source", "Gene");
		Entity koetje2 = new DynamicEntity(expectedMetadata);
		koetje2.set("identifier", "2");
		koetje2.set("height", 4d);
		koetje2.set("source", "Gene");
		Entity koetje3 = new DynamicEntity(expectedMetadata);
		koetje3.set("identifier", "3");
		koetje3.set("height", 6d);
		koetje3.set("source", "Gene");

		assertEquals(created, of(koetje1, koetje2, koetje3));
		verify(permissionSystemService)
				.giveUserEntityPermissions(SecurityContextHolder.getContext(), singletonList(entityName));
	}

	/**
	 * New entities in the source should be added to the target when a new mapping to the same target is performed.
	 */
	@Test
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
		mappingService.applyMappings(project.getMappingTarget("HopEntity"), entityName);

		Repository<Entity> actual = dataService.getRepository(entityName);
		EntityMetaData expectedMetadata = EntityMetaData.newInstance(hopMetaData);
		expectedMetadata.addAttribute(attrMetaFactory.create().setName("source"));
		assertEquals(actual.getEntityMetaData(), expectedMetadata);
		Set<Entity> created = Sets.newHashSet(actual.iterator());

		Entity expected1 = new DynamicEntity(expectedMetadata);
		expected1.set("identifier", "1");
		expected1.set("height", 2d);
		expected1.set("source", "Gene");
		Entity expected2 = new DynamicEntity(expectedMetadata);
		expected2.set("identifier", "2");
		expected2.set("height", 4d);
		expected2.set("source", "Gene");
		Entity expected3 = new DynamicEntity(expectedMetadata);
		expected3.set("identifier", "3");
		expected3.set("height", 6d);
		expected3.set("source", "Gene");
		Entity expected4 = new DynamicEntity(expectedMetadata);
		expected4.set("identifier", "4");
		expected4.set("height", 8d);
		expected4.set("source", "Gene");

		assertEquals(created, of(expected1, expected2, expected3, expected4));
	}

	/**
	 * Applying a mapping multiple times to the same target should update the existing entities.
	 */
	@Test
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
		mappingService.applyMappings(project.getMappingTarget("HopEntity"), entityName);

		Repository<Entity> actual = dataService.getRepository(entityName);
		EntityMetaData expectedMetadata = EntityMetaData.newInstance(hopMetaData);
		expectedMetadata.addAttribute(attrMetaFactory.create().setName("source"));
		assertEquals(actual.getEntityMetaData(), expectedMetadata);
		Set<Entity> created = Sets.newHashSet(actual.iterator());

		Entity expected1 = new DynamicEntity(expectedMetadata);
		expected1.set("identifier", "1");
		expected1.set("height", 2d);
		expected1.set("source", "Gene");
		Entity expected2 = new DynamicEntity(expectedMetadata);
		expected2.set("identifier", "2");
		expected2.set("height", 5.678);
		expected2.set("source", "Gene");
		Entity expected3 = new DynamicEntity(expectedMetadata);
		expected3.set("identifier", "3");
		expected3.set("height", 6d);
		expected3.set("source", "Gene");

		assertEquals(created, of(expected1, expected2, expected3));
	}

	/**
	 * Removing an entity in the source and applying the mapping again should also delete this entity in the target.
	 */
	@Test
	public void testDelete()
	{
		String entityName = "deleteEntity";
		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());

		// make project and apply mappings once
		MappingProject project = createMappingProjectWithTwoSourcesWithMappings(entityName);

		// delete an entity from the source
		dataService.deleteById(geneMetaData.getName(), "2");

		// apply mapping again, this should not delete entities mapped from source 2
		mappingService.applyMappings(project.getMappingTarget("HopEntity"), entityName);

		Repository<Entity> actual = dataService.getRepository(entityName);
		EntityMetaData expectedMetadata = EntityMetaData.newInstance(hopMetaData);
		expectedMetadata.addAttribute(attrMetaFactory.create().setName("source"));
		assertEquals(actual.getEntityMetaData(), expectedMetadata);
		Set<Entity> created = Sets.newHashSet(actual.iterator());

		Entity expected1 = new DynamicEntity(expectedMetadata);
		expected1.set("identifier", "1");
		expected1.set("height", 2d);
		expected1.set("source", "Gene");
		Entity expected3 = new DynamicEntity(expectedMetadata);
		expected3.set("identifier", "3");
		expected3.set("height", 6d);
		expected3.set("source", "Gene");
		Entity expected4 = new DynamicEntity(expectedMetadata);
		expected4.set("identifier", "A");
		expected4.set("height", 12345d);
		expected4.set("source", "Exon");

		assertEquals(created, of(expected1, expected3, expected4));
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testTargetMetaNotCompatible()
	{
		MappingProject project = createMappingProjectWithMappings("compatibleEntity");
		MappingTarget target = project.getMappingTarget("HopEntity");

		// apply mapping to the wrong target
		mappingService.applyMappings(target, geneMetaData.getName());
	}

	private MappingProject createMappingProjectWithMappings(String newEntityName)
	{
		MappingProject mappingProject = mappingService.addMappingProject("TestRun", user, "HopEntity");
		MappingTarget target = mappingProject.getMappingTarget("HopEntity");
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
		MappingProject mappingProject = mappingService.addMappingProject("TestRun", user, "HopEntity");
		MappingTarget target = mappingProject.getMappingTarget("HopEntity");
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

	@Test
	public void testNumericId()
	{
		assertEquals(mappingService.generateId(INT, 1L), "2");
		assertEquals(mappingService.generateId(DECIMAL, 2L), "3");
		assertEquals(mappingService.generateId(LONG, 3L), "4");
	}

	@Test
	public void testStringId()
	{
		reset(idGenerator);
		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());

		mappingService.generateId(STRING, 1L);
		verify(idGenerator).generateId();
	}

	@Configuration
	@ComponentScan({ "org.molgenis.data.mapper.meta", "org.molgenis.auth", "org.molgenis.data.reindex.meta" })
	static class Config
	{
		@Bean
		DataServiceImpl dataService()
		{
			return new DataServiceImpl();
		}

		@Bean
		MetaDataService metaDataService()
		{
			return new MetaDataServiceImpl(dataService(), repositoryCollectionRegistry(),
					systemEntityMetaDataRegistry());
		}

		private SystemEntityMetaDataRegistry systemEntityMetaDataRegistry()
		{
			return new SystemEntityMetaDataRegistry();
		}

		private RepositoryCollectionRegistry repositoryCollectionRegistry()
		{
			return new RepositoryCollectionRegistry(null); // FIXME replace null argument with mocked dependency
		}

		@Bean
		MolgenisUserService userService()
		{
			return mock(MolgenisUserService.class);
		}

		@Bean
		PermissionSystemService permissionSystemService()
		{
			return mock(PermissionSystemService.class);
		}

		@Bean
		SemanticSearchService semanticSearchService()
		{
			return mock(SemanticSearchService.class);
		}

		@Bean
		IdGenerator idGenerator()
		{
			return mock(IdGenerator.class);
		}

		@Bean
		OntologyTagService ontologyTagService()
		{
			return mock(OntologyTagService.class);
		}

		@Bean
		LanguageService languageService()
		{
			return new LanguageService(dataService(), new AppDbSettings());
		}

		@Bean
		ReindexActionRegisterService reindexActionRegisterService()
		{
			return new ReindexActionRegisterServiceImpl();
		}

		@Bean
		FreeMarkerConfigurer freeMarkerConfigurer()
		{
			return new FreeMarkerConfigurer();
		}
	}
}
