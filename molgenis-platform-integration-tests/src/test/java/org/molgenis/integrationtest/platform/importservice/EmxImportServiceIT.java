package org.molgenis.integrationtest.platform.importservice;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.molgenis.data.Entity;
import org.molgenis.data.file.support.FileRepositoryCollection;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.test.context.support.WithMockUser;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.data.DatabaseAction.ADD;
import static org.molgenis.data.DatabaseAction.ADD_UPDATE_EXISTING;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.security.EntityTypePermission.READ;
import static org.molgenis.data.security.EntityTypePermissionUtils.getCumulativePermission;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.testng.Assert.*;

public class EmxImportServiceIT extends ImportServiceIT
{
	private static final String USERNAME = "emx_user";
	private static final String CSV_HOSPITAL = "csv_hospital";
	private static final String CSV_PATIENTS = "csv_patients";
	private static final String TSV_HOSPITAL = "tsv_hospital";
	private static final String TSV_PATIENTS = "tsv_patients";

	@Override
	User getTestUser()
	{
		User user = userFactory.create();
		user.setUsername(USERNAME);
		user.setPassword("password");
		user.setEmail("e@mail.com");
		return user;
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testDoImportEmxCsvZipAsNonSuperuser()
	{
		populateUserPermissions();
		testDoImportEmxCsvZip();
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void testDoImportEmxCsvZipAsSuperuser()
	{
		testDoImportEmxCsvZip();
	}

	private void testDoImportEmxCsvZip()
	{
		String fileName = "emx-csv.zip";
		File file = getFile("/csv/" + fileName);
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport importReport = importService.doImport(repoCollection, ADD, PACKAGE_DEFAULT);
		validateImportReport(importReport, ImmutableMap.of(CSV_HOSPITAL, 3, CSV_PATIENTS, 3),
				ImmutableSet.of(CSV_HOSPITAL, CSV_PATIENTS));
		verifyFirstAndLastRows(CSV_HOSPITAL, hospitalFirstRow, hospitalLastRow);
		verifyFirstAndLastRows(CSV_PATIENTS, patientsFirstRow, patientsLastRow);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testDoImportEmxTsvZipAsNonSuperuser()
	{
		populateUserPermissions();
		testDoImportEmxTsvZip();
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void testDoImportEmxTsvZipAsSuperuser()
	{
		testDoImportEmxTsvZip();
	}

	private void testDoImportEmxTsvZip()
	{
		String fileName = "emx-tsv.zip";
		File file = getFile("/tsv/" + fileName);
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport importReport = importService.doImport(repoCollection, ADD, PACKAGE_DEFAULT);
		validateImportReport(importReport, ImmutableMap.of(TSV_HOSPITAL, 3, TSV_PATIENTS, 3),
				ImmutableSet.of(TSV_HOSPITAL, TSV_PATIENTS));
		verifyFirstAndLastRows(TSV_HOSPITAL, hospitalFirstRow, hospitalLastRow);
		verifyFirstAndLastRows(TSV_PATIENTS, patientsFirstRow, patientsLastRow);
	}

	@DataProvider(name = "doImportEmxAddProvider")
	public Iterator<Object[]> doImportEmxAddProvider()
	{
		List<Object[]> data = new ArrayList<>();

		data.add(createAddData("it_emx_datatypes.xlsx", asList("it", "emx", "datatypes"),
				ImmutableMap.<String, Integer>builder().put("TypeTestRef", 5).put("TypeTest", 38).build(),
				ImmutableSet.of("Person", "TypeTestRef", "Location", "TypeTest"), this::verifyItEmxDataTypes));

		data.add(createAddData("it_emx_deep_nesting.xlsx", asList("it", "deep"), ImmutableMap.<String, Integer>builder()
				.put("TestCategorical1", 50)
				.put("TestMref1", 50)
				.put("TestXref2", 50)
				.put("TestXref1", 50)
				.put("advanced" + PACKAGE_SEPARATOR + "p" + PACKAGE_SEPARATOR + "TestEntity2", 50)
				.build(), ImmutableSet.of("TestCategorical1", "TestMref1", "TestXref2", "TestXref1", "TestEntity0",
				"advanced_TestEntity1", "advanced_p_TestEntity2"), this::verifyItEmxDeepNesting));

		data.add(createAddData("it_emx_lookup_attribute.xlsx", asList("it", "emx", "lookupattribute"),
				ImmutableMap.<String, Integer>builder().put("Ref1", 2)
													   .put("Ref2", 2)
													   .put("Ref3", 2)
													   .put("Ref4", 2)
													   .put("Ref5", 2)
													   .build(),
				ImmutableSet.of("AbstractTop", "AbstractMiddle", "Ref1", "Ref2", "Ref3", "Ref4", "Ref5",
						"TestLookupAttributes"), this::verifyItEmxLookupAttribute));

		data.add(createAddData("it_emx_autoid.xlsx", asList("it", "emx", "autoid"), ImmutableMap.of("testAutoId", 4),
				ImmutableSet.of("testAutoId"), this::verifyItEmxAutoId));

		data.add(createAddData("it_emx_onetomany.xlsx", asList("it", "emx", "onetomany"),
				ImmutableMap.<String, Integer>builder().put("book", 4).put("author", 2).put("node", 4).build(),
				ImmutableSet.of("book", "author", "node"), this::verifyItEmxOneToMany));

		data.add(createAddData("it_emx_self_references.xlsx", asList("it", "emx", "selfreferences"),
				ImmutableMap.of("PersonTest", 11), ImmutableSet.of("PersonTest"), this::verifyItEmxSelfReferences));

		data.add(
				createAddData("it_emx_tags.xlsx", asList("it", "emx", "tags"), emptyMap(), ImmutableSet.of("TagEntity"),
						this::verifyItEmxTags));

		return data.iterator();
	}

	private void verifyItEmxDataTypes()
	{
		verifyFirstAndLastRows("it_emx_datatypes_TypeTest", typeTestFirstRow, typeTestLastRow);
		verifyFirstAndLastRows("it_emx_datatypes_TypeTestRef", typeTestRefFirstRow, typeTestRefLastRow);
	}

	private void verifyItEmxDeepNesting()
	{
		verifyFirstAndLastRows("it_deep_advanced_p_TestEntity2", testEntity2FirstRow, testEntity2LastRow);
		verifyFirstAndLastRows("it_deep_TestCategorical1", testCategorical1FirstRow, testCategorical1LastRow);
		verifyFirstAndLastRows("it_deep_TestXref1", testXref1FirstRow, testXref1LastRow);
		verifyFirstAndLastRows("it_deep_TestXref2", testXref2FirstRow, testXref2LastRow);
		verifyFirstAndLastRows("it_deep_TestMref1", testMref1FirstRow, testMref1LastRow);
	}

	private void verifyItEmxLookupAttribute()
	{
		verifyLookupAttributes("it_emx_lookupattribute_AbstractTop", "lookupTop");
		verifyLookupAttributes("it_emx_lookupattribute_AbstractMiddle", "lookupTop", "lookupMiddle");
		verifyLookupAttributes("it_emx_lookupattribute_Ref1", "lookupTop", "lookupMiddle");
		verifyLookupAttributes("it_emx_lookupattribute_Ref2", "lookupTop", "lookupMiddle", "lookupRef2");
		verifyLookupAttributes("it_emx_lookupattribute_Ref3", "id", "label");
		verifyLookupAttributes("it_emx_lookupattribute_Ref4", "lookupRef4");
		verifyLookupAttributes("it_emx_lookupattribute_Ref5", "lookupRef5Int", "lookupRef5String");
	}

	private void verifyLookupAttributes(String entityName, String... lookupAttributeNames)
	{
		EntityType entityType = dataService.getEntityType(entityName);
		assertEquals(Iterables.size(entityType.getLookupAttributes()), lookupAttributeNames.length);

		for (String lookupAttributeName : lookupAttributeNames)
		{
			assertNotNull(entityType.getLookupAttribute(lookupAttributeName));
		}
	}

	private void verifyItEmxAutoId()
	{
		List<Entity> entities = findAllAsList("it_emx_autoid_testAutoId");
		assertTrue(entities.stream()
						   .anyMatch(entity -> testAutoIdFirstRow.get("firstName").equals(entity.get("firstName"))
								   && testAutoIdFirstRow.get("lastName").equals(entity.get("lastName"))));
		assertTrue(entities.stream()
						   .anyMatch(entity -> testAutoIdLastRow.get("firstName").equals(entity.get("firstName"))
								   && testAutoIdLastRow.get("lastName").equals(entity.get("lastName"))));
	}

	private void verifyItEmxOneToMany()
	{
		verifyFirstAndLastRows("it_emx_onetomany_author", authorFirstRow, authorLastRow);
		verifyFirstAndLastRows("it_emx_onetomany_book", bookFirstRow, bookLastRow);
		verifyFirstAndLastRows("it_emx_onetomany_node", nodeFirstRow, nodeLastRow);
	}

	private void verifyItEmxSelfReferences()
	{
		verifyFirstAndLastRows("it_emx_selfreferences_PersonTest", personTestFirstRow, personTestLastRow);
	}

	private void verifyItEmxTags()
	{
		verifyFirstAndLastRows("sys_md_Tag", tagFirstRow, tagLastRow);

		EntityType entityType = dataService.getEntityType("it_emx_tags_TagEntity");
		Iterable<Tag> entityTags = entityType.getTags();
		assertEquals(getIdsAsSet(entityTags), newHashSet("entitytag0", "entitytag1"));

		Iterable<Tag> idTags = entityType.getAttribute("id").getTags();
		Iterable<Tag> labelTags = entityType.getAttribute("label").getTags();
		assertEquals(getIdsAsSet(idTags), newHashSet("attributetag0", "attributetag1"));
		assertEquals(getIdsAsSet(labelTags), newHashSet("attributetag0", "attributetag1"));

		Package emxPackage = entityType.getPackage();
		Iterable<Tag> packageTags = emxPackage.getTags();
		assertEquals(getIdsAsSet(packageTags), newHashSet("packagetag0", "packagetag1"));
	}

	@Test(dataProvider = "doImportEmxAddProvider")
	@WithMockUser(username = USERNAME)
	public void testDoImportAddEmxAsNonSuperuser(File file, Map<String, Integer> entityCountMap, Set<String> addedEntityTypes, Runnable entityValidationMethod)
	{
		populateUserPermissions();
		testDoImportAddEmx(file, entityCountMap, addedEntityTypes, entityValidationMethod);
	}

	@Test(dataProvider = "doImportEmxAddProvider")
	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	public void testDoImportAddEmxAsSuperuser(File file, Map<String, Integer> entityCountMap,
			Set<String> addedEntityTypes, Runnable entityValidationMethod)
	{
		testDoImportAddEmx(file, entityCountMap, addedEntityTypes, entityValidationMethod);
	}

	private void testDoImportAddEmx(File file, Map<String, Integer> entityCountMap, Set<String> addedEntityTypes,
			Runnable entityValidationMethod)
	{
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);

		EntityImportReport importReport = importService.doImport(repoCollection, ADD, PACKAGE_DEFAULT);

		validateImportReport(importReport, entityCountMap, addedEntityTypes);
		entityValidationMethod.run();
	}

	@DataProvider(name = "doImportEmxAddUpdateProvider")
	public Iterator<Object[]> doImportEmxAddUpdateProvider()
	{
		List<Object[]> data = new ArrayList<>();

		data.add(createUpdateData("it_emx_addupdate.xlsx", "it_emx_addupdate-addupdate.xlsx",
				asList("it", "emx", "addupdate"),
				ImmutableMap.<String, Integer>builder().put("TestAddUpdate", 3).build(), Collections.emptySet(),
				this::verifyItEmxAddUpdate));

		data.add(createUpdateData("it_emx_addAttrToAbstract.xlsx", "it_emx_addAttrToAbstract-update.xlsx",
				asList("it", "emx", "addAttrToAbstract"),
				ImmutableMap.<String, Integer>builder().put("Child1", 2).put("Child2", 2).build(),
				Collections.emptySet(), this::verifyItEmxAttrToAbstract));
		return data.iterator();
	}

	private void verifyItEmxAddUpdate()
	{
		List<Entity> importedEntities = findAllAsList("it_emx_addupdate_TestAddUpdate");

		Set<Map<String, Object>> importedEntityValuesSet = importedEntities.stream()
																		   .map(ImportServiceIT::entityToMap)
																		   .collect(Collectors.toSet());

		List<Map<String, Object>> someExpectedEntityValues = asList(testAddUpdateFirstRow, testAddUpdateSecondRow,
				testAddUpdateLastRow);
		assertTrue(importedEntityValuesSet.containsAll(someExpectedEntityValues));
	}

	private void verifyItEmxAttrToAbstract()
	{
		EntityType parent1 = dataService.getEntityType("it_emx_addAttrToAbstract_Parent1");
		assertNotNull(parent1.getAttribute("newAttr"));
		EntityType parent2 = dataService.getEntityType("it_emx_addAttrToAbstract_Parent2");
		assertNotNull(parent2.getAttribute("newAttr"));
		EntityType child1 = dataService.getEntityType("it_emx_addAttrToAbstract_Child1");
		assertNotNull(child1.getAttribute("newAttr"));
		EntityType child2 = dataService.getEntityType("it_emx_addAttrToAbstract_Child2");
		assertNotNull(child2.getAttribute("newAttr"));
	}

	@Test(dataProvider = "doImportEmxAddUpdateProvider")
	@WithMockUser(username = USERNAME)
	public void testDoImportAddUpdateEmxAsNonSuperuser(File file, File addUpdateFile,
			Map<String, Integer> entityCountMap, Set<String> addedEntityTypes, Runnable entityValidationMethod)
	{
		populateUserPermissions();
		executeAddUpdateOrUpdateTest(file, addUpdateFile, entityCountMap, addedEntityTypes, entityValidationMethod);
	}

	@Test(dataProvider = "doImportEmxAddUpdateProvider")
	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	public void testDoImportAddUpdateEmxAsSuperuser(File file, File addUpdateFile, Map<String, Integer> entityCountMap,
			Set<String> addedEntityTypes, Runnable entityValidationMethod)
	{
		executeAddUpdateOrUpdateTest(file, addUpdateFile, entityCountMap, addedEntityTypes, entityValidationMethod);
	}

	private void executeAddUpdateOrUpdateTest(File file, File addUpdateFile, Map<String, Integer> entityCountMap,
			Set<String> addedEntityTypes, Runnable entityValidationMethod)
	{
		FileRepositoryCollection addRepoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(
				file);
		ImportService addImportService = importServiceFactory.getImportService(file, addRepoCollection);
		addImportService.doImport(addRepoCollection, ADD, PACKAGE_DEFAULT);

		FileRepositoryCollection addUpdateRepoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(
				addUpdateFile);
		ImportService addUpdateImportService = importServiceFactory.getImportService(addUpdateFile,
				addUpdateRepoCollection);
		EntityImportReport importReport = addUpdateImportService.doImport(addUpdateRepoCollection, ADD_UPDATE_EXISTING,
				PACKAGE_DEFAULT);

		validateImportReport(importReport, entityCountMap, addedEntityTypes);
		entityValidationMethod.run();
	}

	@DataProvider(name = "doImportEmxUpdateProvider")
	public Iterator<Object[]> doImportEmxUpdateProvider()
	{
		List<Object[]> data = new ArrayList<>();

		data.add(createUpdateData("it_emx_update.xlsx", "it_emx_update-update.xlsx", asList("it", "emx", "update"),
				ImmutableMap.<String, Integer>builder().put("TestUpdate", 2).build(), Collections.emptySet(),
				this::verifyItEmxUpdate));
		data.add(createUpdateData("it_emx_requiredDefaultValue.xlsx", "it_emx_requiredDefaultValue-update.xlsx",
				asList("it", "emx", "default"), emptyMap(), Collections.emptySet(),
				this::verifyItEmxUpdateDefaultValue));
		return data.iterator();
	}

	private void verifyItEmxUpdateDefaultValue()
	{
		Map<String, Object> expectedRow0 = newHashMap();
		expectedRow0.put("id", "0");
		expectedRow0.put("label", "Test #0");
		expectedRow0.put("bool", true);
		expectedRow0.put("categorical", "ref0");
		expectedRow0.put("categoricalMref", newHashSet("ref0", "ref1"));
		expectedRow0.put("date", LocalDate.of(2017, 10, 26));
		expectedRow0.put("datetime", Instant.parse("1985-08-12T06:12:13Z"));
		expectedRow0.put("decimal", 1.23);
		expectedRow0.put("email", "mail@molgenis.org");
		expectedRow0.put("enum", "enum0");
		expectedRow0.put("html", "<h1>html</h1>");
		expectedRow0.put("hyperlink", "http://www.molgenis.org/");
		expectedRow0.put("int", 5);
		expectedRow0.put("long", 1234567L);
		expectedRow0.put("mref", newHashSet("ref0", "ref1"));
		expectedRow0.put("string", "str");
		expectedRow0.put("text", "text");
		expectedRow0.put("xref", "ref0");

		Map<String, Object> expectedRow1 = newHashMap();
		expectedRow1.put("id", "1");
		expectedRow1.put("label", "Test #1");
		expectedRow1.put("bool", true);
		expectedRow1.put("categorical", "ref0");
		expectedRow1.put("categoricalMref", newHashSet("ref0", "ref1"));
		expectedRow1.put("date", LocalDate.of(2017, 10, 26));
		expectedRow1.put("datetime", Instant.parse("1985-08-12T06:12:13Z"));
		expectedRow1.put("decimal", 1.23);
		expectedRow1.put("email", "mail@molgenis.org");
		expectedRow1.put("enum", "enum0");
		expectedRow1.put("html", "<h1>html</h1>");
		expectedRow1.put("hyperlink", "http://www.molgenis.org/");
		expectedRow1.put("int", 5);
		expectedRow1.put("long", 1234567L);
		expectedRow1.put("mref", newHashSet("ref0", "ref1"));
		expectedRow1.put("string", "str");
		expectedRow1.put("text", "text");
		expectedRow1.put("xref", "ref0");

		verifyFirstAndLastRows("it_emx_default_RequiredDefault", expectedRow0, expectedRow1);
	}

	private void verifyItEmxUpdate()
	{
		verifyFirstAndLastRows("it_emx_update_TestUpdate", testUpdateFirstRow, testUpdateLastRow);
	}

	@Test(dataProvider = "doImportEmxUpdateProvider")
	@WithMockUser(username = USERNAME)
	public void testDoImportUpdateEmxAsNonSuperuser(File file, File updateFile, Map<String, Integer> entityCountMap,
			Set<String> addedEntityTypes, Runnable entityValidationMethod)
	{
		populateUserPermissions();
		executeAddUpdateOrUpdateTest(file, updateFile, entityCountMap, addedEntityTypes, entityValidationMethod);
	}

	@Test(dataProvider = "doImportEmxUpdateProvider")
	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	public void testDoImportUpdateEmxAsSuperuser(File file, File updateFile, Map<String, Integer> entityCountMap,
			Set<String> addedEntityTypes, Runnable entityValidationMethod)
	{
		executeAddUpdateOrUpdateTest(file, updateFile, entityCountMap, addedEntityTypes, entityValidationMethod);
	}

	private static Object[] createAddData(String fileName, List<String> packageTokens,
			Map<String, Integer> entityCountMap, Set<String> entityTypeNames, Runnable entityValidationMethod)
	{
		File file = getFile("/xls/" + fileName);
		String packageName = String.join(PACKAGE_SEPARATOR, packageTokens);
		Map<String, Object> entityTypeCountMap = entityCountMap.entrySet()
															   .stream()
															   .collect(Collectors.toMap(
																	   entry -> packageName + PACKAGE_SEPARATOR
																			   + entry.getKey(), Map.Entry::getValue));
		Set<String> entityTypeFullyQualifiedNames = entityTypeNames.stream()
																   .map(entityName -> packageName + PACKAGE_SEPARATOR
																		   + entityName)
																   .collect(toSet());
		return new Object[] { file, entityTypeCountMap, entityTypeFullyQualifiedNames, entityValidationMethod };
	}

	private static Object[] createUpdateData(String fileName, String updateFileName, List<String> packageTokens,
			Map<String, Integer> entityCountMap, Set<String> entityTypeNames, Runnable entityValidationMethod)
	{
		File addFile = getFile("/xls/" + fileName);
		File updateFile = getFile("/xls/" + updateFileName);
		String packageName = String.join(PACKAGE_SEPARATOR, packageTokens);
		Map<String, Object> entityTypeCountMap = entityCountMap.entrySet()
															   .stream()
															   .collect(Collectors.toMap(
																	   entry -> packageName + PACKAGE_SEPARATOR
																			   + entry.getKey(), Map.Entry::getValue));
		Set<String> entityTypeFullyQualifiedNames = entityTypeNames.stream()
																   .map(entityName -> packageName + PACKAGE_SEPARATOR
																		   + entityName)
																   .collect(toSet());
		return new Object[] { addFile, updateFile, entityTypeCountMap, entityTypeFullyQualifiedNames,
				entityValidationMethod };
	}

	private static Map<String, Object> patientsFirstRow = newHashMap();

	static
	{
		patientsFirstRow.put("patient_id", "1");
		patientsFirstRow.put("patient_name", "John Doe");
		patientsFirstRow.put("patient_hospital", "UMCG");
	}

	private static Map<String, Object> patientsLastRow = newHashMap();

	static
	{
		patientsLastRow.put("patient_id", "3");
		patientsLastRow.put("patient_name", "Unknown");
		patientsLastRow.put("patient_hospital", "VUMC");
	}

	private static Map<String, Object> hospitalFirstRow = newHashMap();

	static
	{
		hospitalFirstRow.put("hospital_name", "UMCG");
		hospitalFirstRow.put("hospital_city", "Groningen");
	}

	private static Map<String, Object> hospitalLastRow = newHashMap();

	static
	{
		hospitalLastRow.put("hospital_name", "VUMC");
		hospitalLastRow.put("hospital_city", "Amsterdam");
	}

	private static Map<String, Object> typeTestFirstRow = newHashMap();

	static
	{
		typeTestFirstRow.put("id", 1);
		typeTestFirstRow.put("xbool", Boolean.TRUE);
		typeTestFirstRow.put("xboolnillable", Boolean.TRUE);
		typeTestFirstRow.put("xcompound_int", 1);
		typeTestFirstRow.put("xcompound_string", "compound strings are here");
		typeTestFirstRow.put("xcategorical_value", "ref1");
		typeTestFirstRow.put("xcategoricalnillable_value", "ref1");
		typeTestFirstRow.put("xcategoricalmref_value", newHashSet("ref1"));
		typeTestFirstRow.put("xcatmrefnillable_value", newHashSet("ref1"));
		typeTestFirstRow.put("xdate", LocalDate.of(1985, 8, 1));
		typeTestFirstRow.put("xdatenillable", LocalDate.of(1985, 8, 1));
		typeTestFirstRow.put("xdatetime", Instant.parse("1985-08-12T06:12:13Z"));
		typeTestFirstRow.put("xdatetimenillable", Instant.parse("1985-08-12T06:12:13Z"));
		typeTestFirstRow.put("xdecimal", 1.23);
		typeTestFirstRow.put("xdecimalnillable", 1.23);
		typeTestFirstRow.put("xemail", "molgenis@gmail.com");
		typeTestFirstRow.put("xemailnillable", "molgenis@gmail.com");
		typeTestFirstRow.put("xenum", "enum1");
		typeTestFirstRow.put("xenumnillable", "enum1");
		typeTestFirstRow.put("xhtml", "<h1>html</h1>");
		typeTestFirstRow.put("xhtmlnillable", "<h1>html</h1>");
		typeTestFirstRow.put("xhyperlink", "http://www.molgenis.org/");
		typeTestFirstRow.put("xhyperlinknillable", "http://www.molgenis.org/");
		typeTestFirstRow.put("xint", 5);
		typeTestFirstRow.put("xintnillable", 1);
		typeTestFirstRow.put("xintrange", 1);
		typeTestFirstRow.put("xintrangenillable", 2);
		typeTestFirstRow.put("xlong", 1L);
		typeTestFirstRow.put("xlongnillable", 1L);
		typeTestFirstRow.put("xlongrange", 2L);
		typeTestFirstRow.put("xlongrangenillable", 2L);
		typeTestFirstRow.put("xmref_value", newHashSet("ref1"));
		typeTestFirstRow.put("xmrefnillable_value", newHashSet("ref1"));
		typeTestFirstRow.put("xstring", "str1");
		typeTestFirstRow.put("xstringnillable", "str1");
		typeTestFirstRow.put("xtext", "text");
		typeTestFirstRow.put("xtextnillable", "text");
		typeTestFirstRow.put("xxref_value", "ref1");
		typeTestFirstRow.put("xxrefnillable_value", "ref1");
		typeTestFirstRow.put("xstring_hidden", "hidden");
		typeTestFirstRow.put("xstringnillable_hidden", "hidden");
		typeTestFirstRow.put("xstring_unique", "str1");
		typeTestFirstRow.put("xint_unique", 1);
		typeTestFirstRow.put("xxref_unique", "ref1");
		typeTestFirstRow.put("xfile", null);
		typeTestFirstRow.put("xcomputedxref", 5);
		typeTestFirstRow.put("xcomputedint", 5);
	}

	private static Map<String, Object> typeTestLastRow = newHashMap();

	static
	{
		typeTestLastRow.put("id", 38);
		typeTestLastRow.put("xbool", true);
		typeTestLastRow.put("xboolnillable", true);
		typeTestLastRow.put("xcompound_int", 38);
		typeTestLastRow.put("xcompound_string", "compound strings are here");
		typeTestLastRow.put("xcategorical_value", "ref3");
		typeTestLastRow.put("xcategoricalnillable_value", "ref2");
		typeTestLastRow.put("xcategoricalmref_value", newHashSet("ref1", "ref2", "ref3"));
		typeTestLastRow.put("xcatmrefnillable_value", newHashSet("ref1", "ref3"));
		typeTestLastRow.put("xdate", LocalDate.of(1985, 8, 1));
		typeTestLastRow.put("xdatenillable", LocalDate.of(2015, 4, 1));
		typeTestLastRow.put("xdatetime", Instant.parse("1985-08-12T06:12:13Z"));
		typeTestLastRow.put("xdatetimenillable", Instant.parse("1985-08-12T06:12:13Z"));
		typeTestLastRow.put("xdecimal", 7.89);
		typeTestLastRow.put("xdecimalnillable", 15.666);
		typeTestLastRow.put("xemail", "molgenis@gmail.com");
		typeTestLastRow.put("xemailnillable", "molgenis@gmail.com");
		typeTestLastRow.put("xenum", "enum2");
		typeTestLastRow.put("xenumnillable", "enum3");
		typeTestLastRow.put("xhtml", "<h1>html</h1>");
		typeTestLastRow.put("xhtmlnillable", "<h1>html 2</h1>");
		typeTestLastRow.put("xhyperlink", "http://www.molgenis.org/");
		typeTestLastRow.put("xhyperlinknillable", "http://www.github.com/");
		typeTestLastRow.put("xint", 1);
		typeTestLastRow.put("xintnillable", 1);
		typeTestLastRow.put("xintrange", 4);
		typeTestLastRow.put("xintrangenillable", 77);
		typeTestLastRow.put("xlong", 3L);
		typeTestLastRow.put("xlongnillable", 12342151234L);
		typeTestLastRow.put("xlongrange", 4L);
		typeTestLastRow.put("xlongrangenillable", 3L);
		typeTestLastRow.put("xmref_value", newHashSet("ref1", "ref2", "ref3"));
		typeTestLastRow.put("xmrefnillable_value", newHashSet());
		typeTestLastRow.put("xstring", "str3");
		typeTestLastRow.put("xstringnillable", "xstringnillable");
		typeTestLastRow.put("xtext", "text");
		typeTestLastRow.put("xtextnillable", "xtextnillable");
		typeTestLastRow.put("xxref_value", "ref3");
		typeTestLastRow.put("xxrefnillable_value", null);
		typeTestLastRow.put("xstring_hidden", "hidden");
		typeTestLastRow.put("xstringnillable_hidden", "xstringhidden");
		typeTestLastRow.put("xstring_unique", "str38");
		typeTestLastRow.put("xint_unique", 38);
		typeTestLastRow.put("xxref_unique", null);
		typeTestLastRow.put("xfile", null);
		typeTestLastRow.put("xcomputedxref", 1);
		typeTestLastRow.put("xcomputedint", 1);
	}

	private static Map<String, Object> typeTestRefFirstRow = newHashMap();

	static
	{
		typeTestRefFirstRow.put("value", "ref1");
		typeTestRefFirstRow.put("label", "label1");
	}

	private static Map<String, Object> typeTestRefLastRow = newHashMap();

	static
	{
		typeTestRefLastRow.put("value", "ref5");
		typeTestRefLastRow.put("label", "label5");
	}

	private static Map<String, Object> testEntity2FirstRow = newHashMap();

	static
	{
		testEntity2FirstRow.put("Identifier", "my_id_1");
		testEntity2FirstRow.put("String_1", "lalala this is a super interesting string");
		testEntity2FirstRow.put("Integer_1", 91);
		testEntity2FirstRow.put("Xref_1", "xref_1");
		testEntity2FirstRow.put("Boolean_1", false);
		testEntity2FirstRow.put("Categorical_1", 1);
		testEntity2FirstRow.put("CompoundXref_1", "xref_1");
		testEntity2FirstRow.put("CompoundBoolean_1", false);
		testEntity2FirstRow.put("Mref_1", newHashSet("mref_1", "mref_50"));
	}

	private static Map<String, Object> testEntity2LastRow = newHashMap();

	static
	{
		testEntity2LastRow.put("Identifier", "my_id_50");
		testEntity2LastRow.put("String_1", "lalala this is a super interesting string");
		testEntity2LastRow.put("Integer_1", 189);
		testEntity2LastRow.put("Xref_1", "xref_50");
		testEntity2LastRow.put("Boolean_1", true);
		testEntity2LastRow.put("Categorical_1", 50);
		testEntity2LastRow.put("CompoundXref_1", "xref_50");
		testEntity2LastRow.put("CompoundBoolean_1", true);
		testEntity2LastRow.put("Mref_1", newHashSet("mref_50", "mref_1"));
	}

	private static Map<String, Object> testCategorical1FirstRow = newHashMap();

	static
	{
		testCategorical1FirstRow.put("id", 1);
		testCategorical1FirstRow.put("value", "This is value 1");
	}

	private static Map<String, Object> testCategorical1LastRow = newHashMap();

	static
	{
		testCategorical1LastRow.put("id", 50);
		testCategorical1LastRow.put("value", "This is value 50");
	}

	private static Map<String, Object> testXref1FirstRow = newHashMap();

	static
	{
		testXref1FirstRow.put("Identifier", "xref_1");
		testXref1FirstRow.put("Date_1", LocalDate.of(2013, 12, 11));
		testXref1FirstRow.put("Xref_2", "thisIsAnId_1");
	}

	private static Map<String, Object> testXref1LastRow = newHashMap();

	static
	{
		testXref1LastRow.put("Identifier", "xref_50");
		testXref1LastRow.put("Date_1", LocalDate.of(2001, 2, 14));
		testXref1LastRow.put("Xref_2", "thisIsAnId_50");
	}

	private static Map<String, Object> testXref2FirstRow = newHashMap();

	static
	{
		testXref2FirstRow.put("Identifier", "thisIsAnId_1");
		testXref2FirstRow.put("Boolean_2", true);
		testXref2FirstRow.put("Mref_2", newHashSet("mref_1", "mref_2", "mref_3"));
	}

	private static Map<String, Object> testXref2LastRow = newHashMap();

	static
	{
		testXref2LastRow.put("Identifier", "thisIsAnId_50");
		testXref2LastRow.put("Boolean_2", true);
		testXref2LastRow.put("Mref_2", newHashSet("mref_5"));
	}

	private static Map<String, Object> testMref1FirstRow = newHashMap();

	static
	{
		testMref1FirstRow.put("Identifier", "mref_1");
		testMref1FirstRow.put("Categorical_2", 1);
	}

	private static Map<String, Object> testMref1LastRow = newHashMap();

	static
	{
		testMref1LastRow.put("Identifier", "mref_50");
		testMref1LastRow.put("Categorical_2", 50);
	}

	private static Map<String, Object> testAutoIdFirstRow = newHashMap();

	static
	{
		testAutoIdFirstRow.put("firstName", "John");
		testAutoIdFirstRow.put("lastName", "Doe");
	}

	private static Map<String, Object> testAutoIdLastRow = newHashMap();

	static
	{
		testAutoIdLastRow.put("firstName", "Bob");
		testAutoIdLastRow.put("lastName", "Doe");
	}

	private static Map<String, Object> authorFirstRow = newHashMap();

	static
	{
		authorFirstRow.put("authorId", "hemingway");
		authorFirstRow.put("name", "Ernest Hemingway");
		authorFirstRow.put("books", newHashSet("oldmansea", "belltolls"));
	}

	private static Map<String, Object> authorLastRow = newHashMap();

	static
	{
		authorLastRow.put("authorId", "orwell");
		authorLastRow.put("name", "George Orwell");
		authorLastRow.put("books", newHashSet("1984", "animalfarm"));
	}

	private static Map<String, Object> bookFirstRow = newHashMap();

	static
	{
		bookFirstRow.put("bookId", "oldmansea");
		bookFirstRow.put("title", "The Old Man and the Sea");
		bookFirstRow.put("author", "hemingway");
	}

	private static Map<String, Object> bookLastRow = newHashMap();

	static
	{
		bookLastRow.put("bookId", "animalfarm");
		bookLastRow.put("title", "Animal Farm");
		bookLastRow.put("author", "orwell");
	}

	private static Map<String, Object> nodeFirstRow = newHashMap();

	static
	{
		nodeFirstRow.put("nodeId", "node");
		nodeFirstRow.put("label", "Node");
		nodeFirstRow.put("parent", "parent");
		nodeFirstRow.put("children", newHashSet("child0", "child1"));
	}

	private static Map<String, Object> nodeLastRow = newHashMap();

	static
	{
		nodeLastRow.put("nodeId", "child1");
		nodeLastRow.put("label", "Child #1");
		nodeLastRow.put("parent", "node");
		nodeLastRow.put("children", newHashSet());
	}

	private static Map<String, Object> personTestFirstRow = newHashMap();

	static
	{
		personTestFirstRow.put("id", "me");
		personTestFirstRow.put("mother", "mom");
		personTestFirstRow.put("father", "dad");
		personTestFirstRow.put("brothers", newHashSet("bro0", "bro1"));
		personTestFirstRow.put("sisters", newHashSet("sis0", "sis1"));
	}

	private static Map<String, Object> personTestLastRow = newHashMap();

	static
	{
		personTestLastRow.put("id", "grandpa_dad");
		personTestLastRow.put("mother", null);
		personTestLastRow.put("father", null);
		personTestLastRow.put("brothers", newHashSet());
		personTestLastRow.put("sisters", newHashSet());
	}

	private static Map<String, Object> tagFirstRow = newHashMap();

	static
	{
		tagFirstRow.put("id", "packagetag0");
		tagFirstRow.put("objectIRI", "http://some.url/package0");
		tagFirstRow.put("label", "Package tag #0");
		tagFirstRow.put("relationIRI", "http://molgenis.org/biobankconnect/instanceOf");
		tagFirstRow.put("relationLabel", "Documentation and Help");
		tagFirstRow.put("codeSystem", "EDAM");
	}

	private static Map<String, Object> tagLastRow = newHashMap();

	static
	{
		tagLastRow.put("id", "attributetag1");
		tagLastRow.put("objectIRI", "http://some.url/attribute1");
		tagLastRow.put("label", "Attribute tag #1");
		tagLastRow.put("relationIRI", "http://molgenis.org/biobankconnect/instanceOf");
		tagLastRow.put("relationLabel", "Documentation and Help");
		tagLastRow.put("codeSystem", "EDAM");
	}

	private static Map<String, Object> testUpdateFirstRow = newHashMap();

	static
	{
		testUpdateFirstRow.put("id", "0");
		testUpdateFirstRow.put("label", "Label #0");
	}

	private static Map<String, Object> testUpdateLastRow = newHashMap();

	static
	{
		testUpdateLastRow.put("id", "1");
		testUpdateLastRow.put("label", "Label #1 – Updated");
	}

	private static Map<String, Object> testAddUpdateFirstRow = newHashMap();

	static
	{
		testAddUpdateFirstRow.put("id", "0");
		testAddUpdateFirstRow.put("label", "Label #0");
	}

	private static Map<String, Object> testAddUpdateSecondRow = newHashMap();

	static
	{
		testAddUpdateSecondRow.put("id", "1");
		testAddUpdateSecondRow.put("label", "Label #1 – Updated");
	}

	private static Map<String, Object> testAddUpdateLastRow = newHashMap();

	static
	{
		testAddUpdateLastRow.put("id", "2");
		testAddUpdateLastRow.put("label", "Label #2");
	}

	@Autowired
	private MutableAclService mutableAclService;

	private void populateUserPermissions()
	{
		Sid sid = new PrincipalSid(SecurityUtils.getCurrentUsername());

		Map<String, EntityTypePermission> entityTypePermissionMap = new HashMap<>();
		entityTypePermissionMap.put("sys_md_Package", READ);
		entityTypePermissionMap.put("sys_md_EntityType", READ);
		entityTypePermissionMap.put("sys_md_Attribute", READ);
		entityTypePermissionMap.put("sys_md_Tag", READ);
		entityTypePermissionMap.put("sys_FileMeta", READ);
		entityTypePermissionMap.put("sys_dec_DecoratorConfiguration", READ);

		runAsSystem(() -> entityTypePermissionMap.forEach((entityTypeId, permission) ->
		{
			MutableAcl acl = (MutableAcl) mutableAclService.readAclById(new EntityTypeIdentity(entityTypeId));
			acl.insertAce(acl.getEntries().size(), getCumulativePermission(permission), sid, true);
			mutableAclService.updateAcl(acl);
		}));
	}
}
