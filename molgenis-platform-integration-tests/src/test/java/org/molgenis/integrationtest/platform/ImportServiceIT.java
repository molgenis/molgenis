package org.molgenis.integrationtest.platform;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.mockito.Mockito;
import org.molgenis.auth.User;
import org.molgenis.auth.UserFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.*;
import org.molgenis.data.csv.CsvDataConfig;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.importer.ImportServiceRegistrar;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.vcf.VcfDataConfig;
import org.molgenis.data.vcf.importer.VcfImporterService;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.framework.ui.MolgenisPluginRegistryImpl;
import org.molgenis.ontology.OntologyDataConfig;
import org.molgenis.ontology.core.config.OntologyTestConfig;
import org.molgenis.ontology.importer.OntologyImportService;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.auth.UserMetaData.USER;
import static org.molgenis.data.DatabaseAction.ADD;
import static org.molgenis.data.DatabaseAction.ADD_UPDATE_EXISTING;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { PlatformITConfig.class, ImportServiceIT.Config.class })
@TestExecutionListeners(listeners = { WithSecurityContextTestExecutionListener.class })
@Transactional
@Rollback
public class ImportServiceIT extends AbstractTransactionalTestNGSpringContextTests
{
	private final static Logger LOG = LoggerFactory.getLogger(ImportServiceIT.class);

	private static final String USERNAME = "user";
	private static final String ROLE_SU = "SU";
	private static final String ROLE_READ_PACKAGE = "ENTITY_READ_sys_md_Package";
	private static final String ROLE_READ_ENTITY_TYPE = "ENTITY_READ_sys_md_EntityType";
	private static final String ROLE_READ_ATTRIBUTE = "ENTITY_READ_sys_md_Attribute";
	private static final String ROLE_READ_TAG = "ENTITY_READ_sys_md_Tag";
	private static final String ROLE_READ_OWNED = "ENTITY_READ_sys_sec_Owned";
	private static final String ROLE_READ_FILE_META = "ENTITY_READ_sys_FileMeta";
	private static final String ROLE_WRITE_ONTOLOGY_TERM_DYNAMIC_ANNOTATION = "ENTITY_WRITE_sys_ont_OntologyTermDynamicAnnotation";
	private static final String ROLE_WRITE_ONTOLOGY_TERM_SYNONYM = "ENTITY_WRITE_sys_ont_OntologyTermSynonym";
	private static final String ROLE_WRITE_ONTOLOGY_TERM_NODE_PATH = "ENTITY_WRITE_sys_ont_OntologyTermNodePath";
	private static final String ROLE_WRITE_ONTOLOGY = "ENTITY_WRITE_sys_ont_Ontology";
	private static final String ROLE_WRITE_ONTOLOGY_TERM = "ENTITY_WRITE_sys_ont_OntologyTerm";

	@Autowired
	private UserFactory userFactory;

	@Autowired
	private DataService dataService;

	@Autowired
	private ImportServiceFactory importServiceFactory;

	@Autowired
	private FileRepositoryCollectionFactory fileRepositoryCollectionFactory;

	@Autowired
	private ImportServiceRegistrar importServiceRegistrar;

	@Autowired
	private DataService dataService;

	@BeforeClass
	public void beforeClass()
	{
		ContextRefreshedEvent contextRefreshedEvent = Mockito.mock(ContextRefreshedEvent.class);
		Mockito.when(contextRefreshedEvent.getApplicationContext()).thenReturn(applicationContext);
		importServiceRegistrar.register(contextRefreshedEvent);

		User user = userFactory.create();
		user.setUsername("user");
		user.setPassword("password");
		user.setEmail("e@mail.com");
		RunAsSystemProxy.runAsSystem(() ->
		{
			dataService.add(USER, user);
		});
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_READ_PACKAGE, ROLE_READ_ENTITY_TYPE, ROLE_READ_ATTRIBUTE })
	@Test
	public void testDoImportEmxCsvZipAsNonSuperuser()
	{
		String fileName = "emx-csv.zip";
		File file = getFile("/csv/" + fileName);
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport importReport = importService.doImport(repoCollection, ADD, PACKAGE_DEFAULT);
		validateImportReport(importReport, ImmutableMap.of("csv_hospital", 3, "csv_patients", 3),
				ImmutableSet.of("csv_hospital", "csv_patients"));
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void testDoImportEmxCsvZipAsSuperuser()
	{
		String fileName = "emx-csv.zip";
		File file = getFile("/csv/" + fileName);
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport importReport = importService.doImport(repoCollection, ADD, PACKAGE_DEFAULT);
		validateImportReport(importReport, ImmutableMap.of("csv_hospital", 3, "csv_patients", 3),
				ImmutableSet.of("csv_hospital", "csv_patients"));
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_READ_PACKAGE, ROLE_READ_ENTITY_TYPE, ROLE_READ_ATTRIBUTE })
	@Test
	public void testDoImportEmxTsvZipAsNonSuperuser()
	{
		String fileName = "emx-tsv.zip";
		File file = getFile("/tsv/" + fileName);
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport importReport = importService.doImport(repoCollection, ADD, PACKAGE_DEFAULT);
		validateImportReport(importReport, ImmutableMap.of("tsv_hospital", 3, "tsv_patients", 3),
				ImmutableSet.of("tsv_hospital", "tsv_patients"));
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void testDoImportEmxTsvZipAsSuperuser()
	{
		String fileName = "emx-tsv.zip";
		File file = getFile("/tsv/" + fileName);
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport importReport = importService.doImport(repoCollection, ADD, PACKAGE_DEFAULT);
		validateImportReport(importReport, ImmutableMap.of("tsv_hospital", 3, "tsv_patients", 3),
				ImmutableSet.of("tsv_hospital", "tsv_patients"));
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_READ_PACKAGE, ROLE_READ_ENTITY_TYPE, ROLE_READ_ATTRIBUTE,
			ROLE_WRITE_ONTOLOGY_TERM_DYNAMIC_ANNOTATION, ROLE_WRITE_ONTOLOGY_TERM_SYNONYM,
			ROLE_WRITE_ONTOLOGY_TERM_NODE_PATH, ROLE_WRITE_ONTOLOGY, ROLE_WRITE_ONTOLOGY_TERM })
	@Test
	public void testDoImportOboAsNonSuperuser()
	{
		String fileName = "ontology-small.obo.zip";
		File file = getFile("/obo/" + fileName);
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport importReport = importService.doImport(repoCollection, ADD, PACKAGE_DEFAULT);
		validateImportReport(importReport, ImmutableMap
						.of("sys_ont_OntologyTermDynamicAnnotation", 0, "sys_ont_OntologyTermSynonym", 5,
								"sys_ont_OntologyTermNodePath", 5, "sys_ont_Ontology", 1, "sys_ont_OntologyTerm", 5),
				emptySet());
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void testDoImportOboAsSuperuser()
	{
		String fileName = "ontology-small.obo.zip";
		File file = getFile("/obo/" + fileName);
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport importReport = importService.doImport(repoCollection, ADD, PACKAGE_DEFAULT);
		validateImportReport(importReport, ImmutableMap
						.of("sys_ont_OntologyTermDynamicAnnotation", 0, "sys_ont_OntologyTermSynonym", 5,
								"sys_ont_OntologyTermNodePath", 5, "sys_ont_Ontology", 1, "sys_ont_OntologyTerm", 5),
				emptySet());
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_READ_PACKAGE, ROLE_READ_ENTITY_TYPE, ROLE_READ_ATTRIBUTE,
			ROLE_WRITE_ONTOLOGY_TERM_DYNAMIC_ANNOTATION, ROLE_WRITE_ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
			ROLE_WRITE_ONTOLOGY_TERM_SYNONYM, ROLE_WRITE_ONTOLOGY_TERM_NODE_PATH, ROLE_WRITE_ONTOLOGY,
			ROLE_WRITE_ONTOLOGY_TERM })
	@Test
	public void testDoImportOwlAsNonSuperuser()
	{
		String fileName = "ontology-small.owl.zip";
		File file = getFile("/owl/" + fileName);
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport importReport = importService.doImport(repoCollection, ADD, PACKAGE_DEFAULT);
		validateImportReport(importReport, ImmutableMap
						.of("sys_ont_OntologyTermDynamicAnnotation", 4, "sys_ont_OntologyTermSynonym", 9,
								"sys_ont_OntologyTermNodePath", 10, "sys_ont_Ontology", 1, "sys_ont_OntologyTerm", 9),
				emptySet());
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void testDoImportOwlAsSuperuser()
	{
		String fileName = "ontology-small.owl.zip";
		File file = getFile("/owl/" + fileName);
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport importReport = importService.doImport(repoCollection, ADD, PACKAGE_DEFAULT);
		validateImportReport(importReport, ImmutableMap
						.of("sys_ont_OntologyTermDynamicAnnotation", 4, "sys_ont_OntologyTermSynonym", 9,
								"sys_ont_OntologyTermNodePath", 10, "sys_ont_Ontology", 1, "sys_ont_OntologyTerm", 9),
				emptySet());
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_READ_PACKAGE, ROLE_READ_ENTITY_TYPE, ROLE_READ_ATTRIBUTE })
	@Test
	public void testDoImportVcfWithoutSamplesAsNonSuperuser()
	{
		String entityId = "variantsWithoutSamples";
		String fileName = entityId + ".vcf";
		File file = getFile("/vcf/" + fileName);
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport importReport = importService.doImport(repoCollection, ADD, PACKAGE_DEFAULT);
		validateImportReport(importReport, ImmutableMap.of(entityId, 10),
				ImmutableSet.of(entityId));

		// Check first and last imported row
		List<Entity> entities = dataService.findAll(entityId).collect(Collectors.toList());

		Entity firstRow = entities.get(0);
		assertEquals(firstRow.getString(VcfAttributes.CHROM), "1");
		assertEquals(firstRow.getInt(VcfAttributes.POS), Integer.valueOf(48554748));
		assertEquals(firstRow.getString(VcfAttributes.ID), ""); // dot is imported as empty
		assertEquals(firstRow.getString(VcfAttributes.REF), "T");
		assertEquals(firstRow.getString(VcfAttributes.ALT), "A");
		assertEquals(firstRow.getString(VcfAttributes.QUAL), "100");
		assertEquals(firstRow.getString(VcfAttributes.FILTER), "PASS");
		assertEquals(firstRow.getString("AA"), "G|||");
		// todo check rest of info string
		//		"AA=G|||;AC=0;AF=0.000199681;AFR_AF=0;AMR_AF=0.0014;AN=6;DP=21572;EAS_AF=0;EUR_AF=0;NS=2504;SAS_AF=0");


		Entity lastRow = entities.get(entities.size() - 1);
		assertEquals(lastRow.getString("#CHROM"), "X");
		assertEquals(Integer.valueOf(100640780), lastRow.getInt("POS"));
		assertEquals(lastRow.getString(VcfAttributes.ID), ""); // dot is imported as empty
		assertEquals(lastRow.getString(VcfAttributes.REF), "A");
		assertEquals(lastRow.getString(VcfAttributes.ALT), "T");
		assertEquals(lastRow.getString(VcfAttributes.QUAL), "100");
		assertEquals(lastRow.getString(VcfAttributes.FILTER), "PASS");
		assertEquals(lastRow.getString("AA"), "G|||");
		// todo check rest of info string
		//		"AA=G|||;AC=0;AF=0.000199681;AFR_AF=0;AMR_AF=0.0014;AN=6;DP=21572;EAS_AF=0;EUR_AF=0;NS=2504;SAS_AF=0");


	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void testDoImportVcfWithoutSamplesAsSuperuser()
	{
		String fileName = "variantsWithoutSamples.vcf";
		File file = getFile("/vcf/" + fileName);
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport importReport = importService.doImport(repoCollection, ADD, PACKAGE_DEFAULT);
		validateImportReport(importReport, ImmutableMap.of("variantsWithoutSamples", 10),
				ImmutableSet.of("variantsWithoutSamples"));
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_READ_PACKAGE, ROLE_READ_ENTITY_TYPE, ROLE_READ_ATTRIBUTE })
	@Test
	public void testDoImportVcfWithSamplesAsNonSuperuser()
	{
		String fileName = "variantsWithSamples.vcf";
		File file = getFile("/vcf/" + fileName);
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport importReport = importService.doImport(repoCollection, ADD, PACKAGE_DEFAULT);
		validateImportReport(importReport, ImmutableMap.of("variantsWithSamples", 10, "variantsWithSamplesSample", 10),
				ImmutableSet.of("variantsWithSamples", "variantsWithSamplesSample"));
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void testDoImportVcfWithSamplesAsSuperuser()
	{
		String fileName = "variantsWithSamples.vcf";
		File file = getFile("/vcf/" + fileName);
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport importReport = importService.doImport(repoCollection, ADD, PACKAGE_DEFAULT);
		validateImportReport(importReport, ImmutableMap.of("variantsWithSamples", 10, "variantsWithSamplesSample", 10),
				ImmutableSet.of("variantsWithSamples", "variantsWithSamplesSample"));
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_READ_PACKAGE, ROLE_READ_ENTITY_TYPE, ROLE_READ_ATTRIBUTE })
	@Test
	public void testDoImportVcfGzWithSamplesAsNonSuperuser()
	{
		String fileName = "variantsWithSamplesGz.vcf.gz";
		File file = getFile("/vcf/" + fileName);
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport importReport = importService.doImport(repoCollection, ADD, PACKAGE_DEFAULT);
		validateImportReport(importReport,
				ImmutableMap.of("variantsWithSamplesGz", 10, "variantsWithSamplesGzSample", 10),
				ImmutableSet.of("variantsWithSamplesGz", "variantsWithSamplesGzSample"));
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void testDoImportVcfGzWithSamplesAsSuperuser()
	{
		String fileName = "variantsWithSamplesGz.vcf.gz";
		File file = getFile("/vcf/" + fileName);
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport importReport = importService.doImport(repoCollection, ADD, PACKAGE_DEFAULT);
		validateImportReport(importReport,
				ImmutableMap.of("variantsWithSamplesGz", 10, "variantsWithSamplesGzSample", 10),
				ImmutableSet.of("variantsWithSamplesGz", "variantsWithSamplesGzSample"));
	}

	@DataProvider(name = "doImportEmxAddProvider")
	public Iterator<Object[]> doImportEmxAddProvider()
	{
		List<Object[]> data = new ArrayList<>();

		data.add(createAddData("it_emx_datatypes.xlsx", asList("it", "emx", "datatypes"),
				ImmutableMap.<String, Integer>builder().put("TypeTestRef", 5).put("TypeTest", 38).build(),
				ImmutableSet.of("Person", "TypeTestRef", "Location", "TypeTest")));

		data.add(createAddData("it_emx_deep_nesting.xlsx", asList("it", "deep"),
				ImmutableMap.<String, Integer>builder().put("TestCategorical1", 50).put("TestMref1", 50)
						.put("TestXref2", 50).put("TestXref1", 50)
						.put("advanced" + PACKAGE_SEPARATOR + "p" + PACKAGE_SEPARATOR + "TestEntity2", 50).build(),
				ImmutableSet.of("TestCategorical1", "TestMref1", "TestXref2", "TestXref1", "TestEntity0",
						"advanced_TestEntity1", "advanced_p_TestEntity2")));

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
	@WithMockUser(username = USERNAME, roles = { ROLE_READ_PACKAGE, ROLE_READ_ENTITY_TYPE, ROLE_READ_ATTRIBUTE,
			ROLE_READ_TAG, ROLE_READ_OWNED, ROLE_READ_FILE_META })
	public void testDoImportAddEmxAsNonSuperuser(File file, Map<String, Integer> entityCountMap,
			Set<String> addedEntityTypes)
	{
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);

		EntityImportReport importReport = importService.doImport(repoCollection, ADD, PACKAGE_DEFAULT);

		validateImportReport(importReport, entityCountMap, addedEntityTypes);
	}

	@Test(dataProvider = "doImportEmxAddProvider")
	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	public void testDoImportAddEmxAsSuperuser(File file, Map<String, Integer> entityCountMap,
			Set<String> addedEntityTypes)
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

		data.add(createUpdateData("it_emx_addAttrToAbstract.xlsx", "it_emx_addAttrToAbstract-update.xlsx",
				asList("it", "emx", "addAttrToAbstract"),
				ImmutableMap.<String, Integer>builder().put("Child1", 2).put("Child2", 2).build(),
				Collections.emptySet()));
		return data.iterator();
	}

	@Test(dataProvider = "doImportEmxAddUpdateProvider")
	@WithMockUser(username = USERNAME, roles = { ROLE_READ_PACKAGE, ROLE_READ_ENTITY_TYPE, ROLE_READ_ATTRIBUTE })
	public void testDoImportAddUpdateEmxAsNonSuperuser(File file, File addUpdateFile,
			Map<String, Integer> entityCountMap, Set<String> addedEntityTypes)
	{
		executeAddUpdateOrUpdateTest(file, addUpdateFile, entityCountMap, addedEntityTypes);
	}

	@Test(dataProvider = "doImportEmxAddUpdateProvider")
	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	public void testDoImportAddUpdateEmxAsSuperuser(File file, File addUpdateFile, Map<String, Integer> entityCountMap,
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
	@WithMockUser(username = USERNAME, roles = { ROLE_READ_PACKAGE, ROLE_READ_ENTITY_TYPE, ROLE_READ_ATTRIBUTE })
	public void testDoImportUpdateEmxAsNonSuperuser(File file, File updateFile, Map<String, Integer> entityCountMap,
			Set<String> addedEntityTypes)
	{
		executeAddUpdateOrUpdateTest(file, updateFile, entityCountMap, addedEntityTypes);
	}

	@Test(dataProvider = "doImportEmxUpdateProvider")
	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	public void testDoImportUpdateEmxAsSuperuser(File file, File updateFile, Map<String, Integer> entityCountMap,
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

	@Import(value = { VcfDataConfig.class, VcfImporterService.class, VcfAttributes.class, OntologyDataConfig.class,
			OntologyTestConfig.class, OntologyImportService.class, MolgenisPluginRegistryImpl.class,
			CsvDataConfig.class })
	static class Config
	{

	}
}
