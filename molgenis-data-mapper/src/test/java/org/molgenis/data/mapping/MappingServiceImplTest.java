package org.molgenis.data.mapping;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.ManageableCrudRepositoryCollection;
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

	@BeforeMethod
	public void beforeTest()
	{
		dataService.getEntityNames().forEach(dataService::removeRepository);
	}

	@Test
	public void testAdd()
	{
		DefaultEntityMetaData targetEntityMetaData = new DefaultEntityMetaData("TargetEntity",
				PackageImpl.defaultPackage);
		targetEntityMetaData.addAttribute("identifier").setIdAttribute(true);

		MolgenisUser user = new MolgenisUser();
		user.setUsername("Piet");

		when(metaDataService.getEntityMetaData("TargetEntity")).thenReturn(targetEntityMetaData);
		when(userService.getUser("Piet")).thenReturn(user);

		// add the project
		MappingProject added = mappingService.addMappingProject("Test123", user, "TargetEntity");
		assertEquals(added.getName(), "Test123");

		System.out.println(added);

		MappingProject expected = new MappingProject("Test123", user);
		expected.addTarget(targetEntityMetaData);

		final String mappingProjectId = added.getIdentifier();
		assertNotNull(mappingProjectId);
		expected.setIdentifier(mappingProjectId);
		final String mappingTargetId = added.getTargets().get("TargetEntity").getIdentifier();
		assertNotNull(mappingTargetId);
		expected.getTargets().get("TargetEntity").setIdentifier(mappingTargetId);
		assertEquals(added, expected);

		MappingProject retrieved = mappingService.getMappingProject(mappingProjectId);
		// Gson gson = new Gson();
		// String retrievedJson = gson.toJson(retrieved);
		// String expectedJson = gson.toJson(expected);
		assertEquals(retrieved, expected);
	}
}
