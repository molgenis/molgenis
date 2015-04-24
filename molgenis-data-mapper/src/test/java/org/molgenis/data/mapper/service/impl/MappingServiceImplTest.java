package org.molgenis.data.mapper.service.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.DECIMAL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.elasticsearch.common.collect.Lists;
import org.mockito.Mockito;
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

	private MolgenisUser user;

	private DefaultEntityMetaData hopMetaData;

	private DefaultEntityMetaData geneMetaData;

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

	@Test
	public void testAddTarget()
	{
		MappingProject mappingProject = mappingService.addMappingProject("Test123", user, "HopEntity");
		mappingProject.addTarget(geneMetaData);
		mappingService.updateMappingProject(mappingProject);

		MappingProject retrieved = mappingService.getMappingProject(mappingProject.getIdentifier());
		assertEquals(mappingProject, retrieved);
	}

	@Test
	public void testAddExistingTarget()
	{
		MappingProject mappingProject = mappingService.addMappingProject("Test123", user, "HopEntity");
		try
		{
			mappingProject.addTarget(hopMetaData);
			fail("Cannot add same target twice");
		}
		catch (Exception expected)
		{

		}
	}

	@Test
	public void testAddNewSource()
	{
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
		MappingProject mappingProject = mappingService.addMappingProject("TestRun", user, "HopEntity");
		MappingTarget target = mappingProject.getMappingTarget("HopEntity");
		EntityMapping mapping = target.addSource(geneMetaData);
		AttributeMapping attrMapping = mapping.addAttributeMapping("hoogte");
		attrMapping.setAlgorithm("$('lengte').value()");

		mappingService.applyMappings(target, "Koetjeboe");

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

		Mockito.verify(permissionSystemService).giveUserEntityAndMenuPermissions(SecurityContextHolder.getContext(),
				Arrays.asList("Koetjeboe"));
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
			return new UuidGenerator();
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
			ManageableRepositoryCollection manageableRepositoryCollection = manageableRepositoryCollection();
			metaDataService.setDefaultBackend(manageableRepositoryCollection);
			metaDataService.addEntityMeta(AttributeMappingRepositoryImpl.META_DATA);
			metaDataService.addEntityMeta(EntityMappingRepositoryImpl.META_DATA);
			metaDataService.addEntityMeta(MappingTargetRepositoryImpl.META_DATA);
			metaDataService.addEntityMeta(MappingProjectRepositoryImpl.META_DATA);
		}
	}
}
