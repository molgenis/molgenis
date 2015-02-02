package org.molgenis.data.mapping;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.mapping.model.EntityMapping;
import org.molgenis.data.mapping.model.MappingProject;
import org.molgenis.data.mem.InMemoryRepositoryCollection;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.PackageImpl;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.security.user.MolgenisUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes =
{ MappingServiceImplTest.Config.class, MappingConfig.class })
public class MappingServiceImplTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	static class Config
	{
		@Bean
		DataService dataService()
		{
			return new DataServiceImpl();
		}

		@Bean
		MetaDataService metaDataService()
		{
			return mock(MetaDataService.class);
		}

		@Bean
		MolgenisUserService userService()
		{
			return mock(MolgenisUserService.class);
		}

		@Bean
		ManageableCrudRepositoryCollection repositoryCollection()
		{
			return new InMemoryRepositoryCollection(dataService());
		}

	}

	@Autowired
	private ManageableCrudRepositoryCollection repoCollection;

	@Autowired
	private DataService dataService;

	@Autowired
	private MappingService mappingService;

	@Autowired
	private MetaDataService metaDataService;

	@Autowired
	private MolgenisUserService userService;

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
		when(metaDataService.getEntityMetaData("HopEntity")).thenReturn(hopMetaData);

		geneMetaData = new DefaultEntityMetaData("Gene", PackageImpl.defaultPackage);
		geneMetaData.addAttribute("Gene").setIdAttribute(true);
		when(metaDataService.getEntityMetaData("Gene")).thenReturn(geneMetaData);

		dataService.getEntityNames().forEach(dataService::removeRepository);
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
}
