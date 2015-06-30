package org.molgenis.data.mapper.service.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.DECIMAL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.Entity;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Repository;
import org.molgenis.data.mapper.config.MappingConfig;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.mapping.model.MappingProject;
import org.molgenis.data.mapper.mapping.model.MappingTarget;
import org.molgenis.data.mapper.repository.MappingProjectRepository;
import org.molgenis.data.mapper.repository.impl.AttributeMappingRepositoryImpl;
import org.molgenis.data.mapper.repository.impl.EntityMappingRepositoryImpl;
import org.molgenis.data.mapper.repository.impl.MappingProjectRepositoryImpl;
import org.molgenis.data.mapper.repository.impl.MappingTargetRepositoryImpl;
import org.molgenis.data.mapper.service.MappingService;
import org.molgenis.data.mem.InMemoryRepositoryCollection;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.meta.PackageImpl;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.security.user.MolgenisUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

@ContextConfiguration(classes =
{ MappingServiceImplTest.Config.class, MappingConfig.class })
public class MappingServiceImplTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private ManageableRepositoryCollection repoCollection;

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

	@Autowired
	private MappingProjectRepository mappingProjectRepository;

	private MolgenisUser user;

	private DefaultEntityMetaData hopMetaData;

	private DefaultEntityMetaData geneMetaData;

	private UuidGenerator uuidGenerator = new UuidGenerator();

	@BeforeMethod
	public void beforeMethod()
	{
		user = new MolgenisUser();
		user.setUsername("Piet");
		when(userService.getUser("Piet")).thenReturn(user);

		hopMetaData = new DefaultEntityMetaData("HopEntity", PackageImpl.defaultPackage);
		hopMetaData.addAttribute("identifier").setIdAttribute(true);
		hopMetaData.addAttribute("hoogte").setDataType(DECIMAL).setNillable(false);

		geneMetaData = new DefaultEntityMetaData("Gene", PackageImpl.defaultPackage);
		geneMetaData.addAttribute("id").setIdAttribute(true);
		geneMetaData.addAttribute("lengte").setDataType(DECIMAL).setNillable(false);

		if (!dataService.hasRepository("HopEntity"))
		{
			dataService.getMeta().addEntityMeta(hopMetaData);
			Repository gene = dataService.getMeta().addEntityMeta(geneMetaData);
			MapEntity geneEntity = new MapEntity(geneMetaData);
			geneEntity.set("id", "1");
			geneEntity.set("lengte", 123.4);
			gene.add(geneEntity);
		}

		dataService.getEntityNames().forEach(dataService::removeRepository);

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

		MappingProject clonedMappingProject = mappingService.cloneMappingProject(mappingProject.getIdentifier(),
				"Clone of TestRun");

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
				assertEquals(entityMapping.getSourceEntityMetaData().getName(), clonedEntityMapping
						.getSourceEntityMetaData().getName());
				assertEquals(entityMapping.getTargetEntityMetaData().getName(), clonedEntityMapping
						.getTargetEntityMetaData().getName());

				List<AttributeMapping> attributeMappings = Lists.newArrayList(entityMapping.getAttributeMappings());
				List<AttributeMapping> clonedAttributeMappings = Lists.newArrayList(clonedEntityMapping
						.getAttributeMappings());
				assertEquals(attributeMappings.size(), clonedAttributeMappings.size());

				for (int k = 0; k < attributeMappings.size(); ++k)
				{
					AttributeMapping attributeMapping = attributeMappings.get(k);
					AttributeMapping clonedAttributeMapping = clonedAttributeMappings.get(k);
					assertNotEquals(attributeMapping.getIdentifier(), clonedAttributeMapping.getIdentifier());

					assertEquals(attributeMapping.getAlgorithm(), clonedAttributeMapping.getAlgorithm());
					assertEquals(attributeMapping.getTargetAttributeMetaData().getName(), clonedAttributeMapping
							.getTargetAttributeMetaData().getName());
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
		catch (IllegalStateException expected)
		{
		}
	}

	@Test
	public void testApplyMappings()
	{
		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());
		createMappingProjectWithMappings("Koetjeboe");

		Repository actual = dataService.getRepository("Koetjeboe");
		DefaultEntityMetaData expectedMetadata = new DefaultEntityMetaData("Koetjeboe", hopMetaData);
		expectedMetadata.addAttribute("source");
		assertEquals(actual.getEntityMetaData(), expectedMetadata);
		List<Entity> created = Lists.newArrayList(actual.iterator());

		MapEntity koetje = new MapEntity(expectedMetadata);

		String identifier = created.get(0).getString("identifier");
		assertNotNull(identifier);
		koetje.set("identifier", identifier);
		koetje.set("hoogte", new Double(123.4));
		koetje.set("source", "Gene");
		assertEquals(created, ImmutableList.<Entity> of(koetje));

		verify(permissionSystemService).giveUserEntityPermissions(SecurityContextHolder.getContext(),
				Arrays.asList("Koetjeboe"));
	}

	private MappingProject createMappingProjectWithMappings(String newEntityName)
	{
		MappingProject mappingProject = mappingService.addMappingProject("TestRun", user, "HopEntity");
		MappingTarget target = mappingProject.getMappingTarget("HopEntity");
		EntityMapping mapping = target.addSource(geneMetaData);
		AttributeMapping attrMapping = mapping.addAttributeMapping("hoogte");
		attrMapping.setAlgorithm("$('lengte').value()");

		mappingService.applyMappings(target, newEntityName);
		return mappingProject;
	}

	@Test
	public void testNumericId()
	{
		assertEquals(mappingService.generateId(MolgenisFieldTypes.INT, 1l), "2");
		assertEquals(mappingService.generateId(MolgenisFieldTypes.DECIMAL, 2l), "3");
		assertEquals(mappingService.generateId(MolgenisFieldTypes.LONG, 3l), "4");
	}

	@Test
	public void testStringId()
	{
		reset(idGenerator);
		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());

		mappingService.generateId(MolgenisFieldTypes.STRING, 1l);
		verify(idGenerator).generateId();
	}

	@Configuration
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
			return new MetaDataServiceImpl(dataService());
		}

		@Bean
		MolgenisUserService userService()
		{
			return mock(MolgenisUserService.class);
		}

		@Bean
		ManageableRepositoryCollection manageableRepositoryCollection()
		{
			return new InMemoryRepositoryCollection("mem");
		}

		@Bean
		PermissionSystemService permissionSystemService()
		{
			return mock(PermissionSystemService.class);
		}

		@Bean
		IdGenerator idGenerator()
		{
			IdGenerator idGenerator = mock(IdGenerator.class);
			return idGenerator;
		}

		@Bean
		SemanticSearchService semanticSearchService()
		{
			return mock(SemanticSearchService.class);
		}

		@PostConstruct
		public void initRepositories()
		{
			MetaDataService metaDataService = metaDataService();
			dataService().setMeta(metaDataService);

			ManageableRepositoryCollection manageableRepositoryCollection = manageableRepositoryCollection();
			metaDataService.setDefaultBackend(manageableRepositoryCollection);
			metaDataService.addEntityMeta(AttributeMappingRepositoryImpl.META_DATA);
			metaDataService.addEntityMeta(EntityMappingRepositoryImpl.META_DATA);
			metaDataService.addEntityMeta(MappingTargetRepositoryImpl.META_DATA);
			metaDataService.addEntityMeta(MappingProjectRepositoryImpl.META_DATA);
		}
	}
}
