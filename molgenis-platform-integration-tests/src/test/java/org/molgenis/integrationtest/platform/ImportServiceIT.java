package org.molgenis.integrationtest.platform;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.mockito.Mockito;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.importer.ImportServiceRegistrar;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.data.DatabaseAction.ADD;
import static org.molgenis.data.DatabaseAction.ADD_UPDATE_EXISTING;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.testng.Assert.assertEquals;

/**
 * Test the Importer test cases
 */
@Rollback(value = false)
@ContextConfiguration(classes = { PlatformITConfig.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class ImportServiceIT extends AbstractTestNGSpringContextTests
{
	private final static Logger LOG = LoggerFactory.getLogger(ImportServiceIT.class);

	@Autowired
	private ImportServiceFactory importServiceFactory;

	@Autowired
	private FileRepositoryCollectionFactory fileRepositoryCollectionFactory;

	@Autowired
	private ImportServiceRegistrar importServiceRegistrar;

	@BeforeClass
	public void beforeClass()
	{
		ContextRefreshedEvent contextRefreshedEvent = Mockito.mock(ContextRefreshedEvent.class);
		Mockito.when(contextRefreshedEvent.getApplicationContext()).thenReturn(applicationContext);
		importServiceRegistrar.register(contextRefreshedEvent);
	}

	@DataProvider(name = "doImportEmxAddProvider")
	public Iterator<Object[]> doImportEmxAddProvider()
	{
		List<Object[]> data = new ArrayList<>();

		data.add(createAddData("it_emx_datatypes.xlsx", asList("it", "emx", "datatypes"),
				ImmutableMap.<String, Integer>builder().put("TypeTestRef", 5).put("TypeTest", 38).build(),
				ImmutableSet.of("Person", "TypeTestRef", "Location", "TypeTest")));

		data.add(createAddData("it_emx_deep_nesting.xlsx", asList("it", "deep"),
				ImmutableMap.<String, Integer>builder().put("TestCategorical_1", 50).put("TestMref_1", 50)
						.put("TestXref_2", 50).put("TestXref_1", 50)
						.put("advanced" + PACKAGE_SEPARATOR + "p" + PACKAGE_SEPARATOR + "TestEntity_2", 50).build(),
				ImmutableSet.of("TestCategorical_1", "TestMref_1", "TestXref_2", "TestXref_1", "TestEntity_0",
						"advanced_TestEntity_1", "advanced_p_TestEntity_2")));

		data.add(createAddData("it_emx_lookup_attribute.xlsx", asList("it", "emx", "lookupattribute"),
				ImmutableMap.<String, Integer>builder().put("Ref1", 2).put("Ref2", 2).put("Ref3", 2).put("Ref4", 2)
						.put("Ref5", 2).build(), ImmutableSet
						.of("AbstractTop", "AbstractMiddle", "Ref1", "Ref2", "Ref3", "Ref4", "Ref5",
								"TestLookupAttributes")));

		data.add(createAddData("it_emx_autoid.xlsx", asList("it", "emx", "autoid"), ImmutableMap.of("testAutoId", 4),
				ImmutableSet.of("testAutoId")));

		data.add(createAddData("it_emx_onetomany.xlsx", asList("it", "emx", "onetomany"),
				ImmutableMap.<String, Integer>builder().put("book", 4).put("author", 2).put("node", 4).build(),
				ImmutableSet.of("book", "author", "node")));

		data.add(createAddData("it_emx_self_references.xlsx", asList("it", "emx", "selfreferences"),
				ImmutableMap.of("PersonTest", 11), ImmutableSet.of("PersonTest")));

		data.add(createAddData("it_emx_tags.xlsx", asList("it", "emx", "tags"), emptyMap(),
				ImmutableSet.of("TagEntity")));

		return data.iterator();
	}

	@Test(dataProvider = "doImportEmxAddProvider")
	@WithMockUser(username = "SYSTEM", authorities = { "ROLE_SYSTEM" })
	public void testDoImportAddEmx(File file, Map<String, Integer> entityCountMap, Set<String> addedEntityTypes)
	{
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);

		EntityImportReport importReport = importService.doImport(repoCollection, ADD, PACKAGE_DEFAULT);

		validateImportReport(importReport, entityCountMap, addedEntityTypes);
	}

	@DataProvider(name = "doImportEmxAddUpdateProvider")
	public Iterator<Object[]> doImportEmxAddUpdateProvider()
	{
		List<Object[]> data = new ArrayList<>();

		data.add(createUpdateData("it_emx_addupdate.xlsx", "it_emx_addupdate-addupdate.xlsx",
				asList("it", "emx", "addupdate"),
				ImmutableMap.<String, Integer>builder().put("TestAddUpdate", 3).build(), Collections.emptySet()));

		return data.iterator();
	}

	@Test(dataProvider = "doImportEmxAddUpdateProvider")
	@WithMockUser(username = "SYSTEM", authorities = { "ROLE_SYSTEM" })
	public void testDoImportAddUpdateEmx(File file, File addUpdateFile, Map<String, Integer> entityCountMap,
			Set<String> addedEntityTypes)
	{
		executeAddUpdateOrUpdateTest(file, addUpdateFile, entityCountMap, addedEntityTypes);
	}

	private void executeAddUpdateOrUpdateTest(File file, File addUpdateFile, Map<String, Integer> entityCountMap,
			Set<String> addedEntityTypes)
	{
		FileRepositoryCollection addRepoCollection = fileRepositoryCollectionFactory
				.createFileRepositoryCollection(file);
		ImportService addImportService = importServiceFactory.getImportService(file, addRepoCollection);
		addImportService.doImport(addRepoCollection, ADD, PACKAGE_DEFAULT);

		FileRepositoryCollection addUpdateRepoCollection = fileRepositoryCollectionFactory
				.createFileRepositoryCollection(addUpdateFile);
		ImportService addUpdateImportService = importServiceFactory
				.getImportService(addUpdateFile, addUpdateRepoCollection);
		EntityImportReport importReport = addUpdateImportService
				.doImport(addUpdateRepoCollection, ADD_UPDATE_EXISTING, PACKAGE_DEFAULT);

		validateImportReport(importReport, entityCountMap, addedEntityTypes);
	}

	@DataProvider(name = "doImportEmxUpdateProvider")
	public Iterator<Object[]> doImportEmxUpdateProvider()
	{
		List<Object[]> data = new ArrayList<>();

		data.add(createUpdateData("it_emx_update.xlsx", "it_emx_update-update.xlsx", asList("it", "emx", "update"),
				ImmutableMap.<String, Integer>builder().put("TestUpdate", 2).build(), Collections.emptySet()));

		return data.iterator();
	}

	@Test(dataProvider = "doImportEmxUpdateProvider")
	@WithMockUser(username = "SYSTEM", authorities = { "ROLE_SYSTEM" })
	public void testDoImportUpdateEmx(File file, File updateFile, Map<String, Integer> entityCountMap,
			Set<String> addedEntityTypes)
	{
		executeAddUpdateOrUpdateTest(file, updateFile, entityCountMap, addedEntityTypes);
	}

	@DataProvider(name = "doImportEmxUpdateAsNonSuperuserProvider")
	public Iterator<Object[]> doImportEmxUpdateAsNonSuperuserProvider()
	{
		List<Object[]> data = new ArrayList<>();

		data.add(createUpdateData("it_emx_nonsu_update.xlsx", "it_emx_nonsu_update-update.xlsx",
				asList("it", "emx", "nonsu", "update"),
				ImmutableMap.<String, Integer>builder().put("TestUpdate", 2).build(), Collections.emptySet()));

		return data.iterator();
	}

	@Test(dataProvider = "doImportEmxUpdateAsNonSuperuserProvider")
	@WithMockUser(username = "user", authorities = { "ROLE_SU" })
	public void testDoImportUpdateAsNonSuperuserEmx(File file, File updateFile, Map<String, Integer> entityCountMap,
			Set<String> addedEntityTypes)
	{
		executeAddUpdateOrUpdateTest(file, updateFile, entityCountMap, addedEntityTypes);
	}

	private static Object[] createAddData(String fileName, List<String> packageTokens,
			Map<String, Integer> entityCountMap, Set<String> entityTypeNames)
	{
		File file = getFile("/xls/" + fileName);
		String packageName = String.join(PACKAGE_SEPARATOR, packageTokens);
		Map<String, Object> entityTypeCountMap = entityCountMap.entrySet().stream().collect(
				Collectors.toMap(entry -> packageName + PACKAGE_SEPARATOR + entry.getKey(), Map.Entry::getValue));
		Set<String> entityTypeFullyQualifiedNames = entityTypeNames.stream()
				.map(entityName -> packageName + PACKAGE_SEPARATOR + entityName).collect(toSet());
		return new Object[] { file, entityTypeCountMap, entityTypeFullyQualifiedNames };
	}

	private static Object[] createUpdateData(String fileName, String updateFileName, List<String> packageTokens,
			Map<String, Integer> entityCountMap, Set<String> entityTypeNames)
	{
		File addFile = getFile("/xls/" + fileName);
		File updateFile = getFile("/xls/" + updateFileName);
		String packageName = String.join(PACKAGE_SEPARATOR, packageTokens);
		Map<String, Object> entityTypeCountMap = entityCountMap.entrySet().stream().collect(
				Collectors.toMap(entry -> packageName + PACKAGE_SEPARATOR + entry.getKey(), Map.Entry::getValue));
		Set<String> entityTypeFullyQualifiedNames = entityTypeNames.stream()
				.map(entityName -> packageName + PACKAGE_SEPARATOR + entityName).collect(toSet());
		return new Object[] { addFile, updateFile, entityTypeCountMap, entityTypeFullyQualifiedNames };
	}

	private static void validateImportReport(EntityImportReport importReport, Map<String, Integer> entityTypeCountMap,
			Set<String> addedEntityTypeIds)
	{
		assertEquals(ImmutableSet.copyOf(importReport.getNewEntities()), addedEntityTypeIds);
		assertEquals(importReport.getNrImportedEntitiesMap(), entityTypeCountMap);
	}

	private static File getFile(String resourceName)
	{
		requireNonNull(resourceName);

		try
		{
			File file = ResourceUtils.getFile(ImportServiceIT.class, resourceName);
			LOG.trace("emx import integration test file: [{}]", file);
			return file;
		}
		catch (Exception e)
		{
			LOG.error("File name: [{}]", resourceName);
			throw new MolgenisDataAccessException(e);
		}
	}
}
