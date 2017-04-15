package org.molgenis.integrationtest.platform;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.mockito.Mockito;
import org.molgenis.auth.User;
import org.molgenis.auth.UserFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.csv.CsvDataConfig;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.importer.ImportServiceRegistrar;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.support.FileRepositoryCollection;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.auth.UserMetaData.USER;
import static org.molgenis.data.DatabaseAction.ADD;
import static org.molgenis.data.DatabaseAction.ADD_UPDATE_EXISTING;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.util.EntityUtils.asStream;
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

	private static final String CSV_HOSPITAL = "csv_hospital";
	private static final String CSV_PATIENTS = "csv_patients";
	private static final String TSV_HOSPITAL = "tsv_hospital";
	private static final String TSV_PATIENTS = "tsv_patients";

	@Autowired
	private UserFactory userFactory;

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
		RunAsSystemProxy.runAsSystem(() -> dataService.add(USER, user));
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_READ_PACKAGE, ROLE_READ_ENTITY_TYPE, ROLE_READ_ATTRIBUTE })
	@Test
	public void testDoImportEmxCsvZipAsNonSuperuser()
	{
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
		validateZipHospitalEntity(CSV_HOSPITAL);
		validateZipPatientEntity(CSV_PATIENTS);
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_READ_PACKAGE, ROLE_READ_ENTITY_TYPE, ROLE_READ_ATTRIBUTE })
	@Test
	public void testDoImportEmxTsvZipAsNonSuperuser()
	{
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
		validateZipHospitalEntity(TSV_HOSPITAL);
		validateZipPatientEntity(TSV_PATIENTS);
	}

	private void validateZipPatientEntity(String patientEntityId)
	{
		List<Entity> patients = dataService.findAll(patientEntityId).collect(Collectors.toList());
		Entity firstPatient = patients.get(0);
		Entity lastPatient = getLast(patients);

		assertEquals(firstPatient.getString("patient_id"), "1");
		assertEquals(firstPatient.getString("patient_name"), "John Doe");
		assertEquals(firstPatient.getEntity("patient_hospital").getIdValue(), "UMCG");
		assertEquals(lastPatient.getString("patient_id"), "3");
		assertEquals(lastPatient.getString("patient_name"), "Unknown");
		assertEquals(lastPatient.getEntity("patient_hospital").getIdValue(), "VUMC");
	}

	private void validateZipHospitalEntity(String hospitalEntityId)
	{
		List<Entity> hospitals = dataService.findAll(hospitalEntityId).collect(Collectors.toList());
		Entity firstHospital = hospitals.get(0);
		Entity lastHospital = getLast(hospitals);

		assertEquals(firstHospital.getString("hospital_name"), "UMCG");
		assertEquals(firstHospital.getString("hospital_city"), "Groningen");
		assertEquals(lastHospital.getString("hospital_name"), "VUMC");
		assertEquals(lastHospital.getString("hospital_city"), "Amsterdam");
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_READ_PACKAGE, ROLE_READ_ENTITY_TYPE, ROLE_READ_ATTRIBUTE,
			ROLE_WRITE_ONTOLOGY_TERM_DYNAMIC_ANNOTATION, ROLE_WRITE_ONTOLOGY_TERM_SYNONYM,
			ROLE_WRITE_ONTOLOGY_TERM_NODE_PATH, ROLE_WRITE_ONTOLOGY, ROLE_WRITE_ONTOLOGY_TERM })
	@Test
	public void testDoImportOboAsNonSuperuser()
	{
		testDoImportObo();
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void testDoImportOboAsSuperuser()
	{
		testDoImportObo();
	}

	private void testDoImportObo()
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
		testDoImportOwl();
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void testDoImportOwlAsSuperuser()
	{
		testDoImportOwl();
	}

	private void testDoImportOwl()
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
		testDoImportVcfWithoutSamples();
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void testDoImportVcfWithoutSamplesAsSuperuser()
	{
		testDoImportVcfWithoutSamples();
	}

	private void testDoImportVcfWithoutSamples()
	{
		String entityId = "variantsWithoutSamples";
		String fileName = entityId + ".vcf";
		File file = getFile("/vcf/" + fileName);
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport importReport = importService.doImport(repoCollection, ADD, PACKAGE_DEFAULT);
		validateImportReport(importReport, ImmutableMap.of(entityId, 10), ImmutableSet.of(entityId));

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
		//	Verify info "AA=G|||;AC=0;AF=0.000199681;AFR_AF=0;AMR_AF=0.0014;AN=6;DP=21572;EAS_AF=0;EUR_AF=0;NS=2504;SAS_AF=0");
		assertEquals(firstRow.getString("AA"), "G|||");
		assertEquals(firstRow.getString("AC"), "0");
		assertEquals(firstRow.getString("AF"), "1.99681E-4");
		assertEquals(firstRow.getString("AFR_AF"), "0.0");
		assertEquals(firstRow.getString("AMR_AF"), "0.0014");
		assertEquals(firstRow.getInt("AN"), Integer.valueOf(6));
		assertEquals(firstRow.getInt("DP"), Integer.valueOf(21572));
		assertEquals(firstRow.getString("EAS_AF"), "0.0");
		assertEquals(firstRow.getString("EUR_AF"), "0.0");
		assertEquals(firstRow.getInt("NS"), Integer.valueOf(2504));
		assertEquals(firstRow.getString("SAS_AF"), "0.0");

		Entity lastRow = entities.get(entities.size() - 1);
		assertEquals(lastRow.getString("#CHROM"), "X");
		assertEquals(Integer.valueOf(100640780), lastRow.getInt("POS"));
		assertEquals(lastRow.getString(VcfAttributes.ID), ""); // dot is imported as empty
		assertEquals(lastRow.getString(VcfAttributes.REF), "A");
		assertEquals(lastRow.getString(VcfAttributes.ALT), "T");
		assertEquals(lastRow.getString(VcfAttributes.QUAL), "100");
		assertEquals(lastRow.getString(VcfAttributes.FILTER), "PASS");
		// Verify info "AA=G|||;AC=0;AF=0.000199681;AFR_AF=0;AMR_AF=0.0014;AN=6;DP=21572;EAS_AF=0;EUR_AF=0;NS=2504;SAS_AF=0");
		assertEquals(lastRow.getString("AA"), "G|||");
		assertEquals(lastRow.getString("AC"), "0");
		assertEquals(lastRow.getString("AF"), "1.99681E-4");
		assertEquals(lastRow.getString("AFR_AF"), "0.0");
		assertEquals(lastRow.getString("AMR_AF"), "0.0014");
		assertEquals(lastRow.getInt("AN"), Integer.valueOf(6));
		assertEquals(lastRow.getInt("DP"), Integer.valueOf(21572));
		assertEquals(lastRow.getString("EAS_AF"), "0.0");
		assertEquals(lastRow.getString("EUR_AF"), "0.0");
		assertEquals(lastRow.getInt("NS"), Integer.valueOf(2504));
		assertEquals(lastRow.getString("SAS_AF"), "0.0");
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_READ_PACKAGE, ROLE_READ_ENTITY_TYPE, ROLE_READ_ATTRIBUTE })
	@Test
	public void testDoImportVcfWithSamplesAsNonSuperuser()
	{
		testDoImportVcfWithSamples();
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void testDoImportVcfWithSamplesAsSuperuser()
	{
		testDoImportVcfWithSamples();
	}

	private void testDoImportVcfWithSamples()
	{
		String entityId = "variantsWithSamples";
		String fileName = entityId + ".vcf";
		File file = getFile("/vcf/" + fileName);
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport importReport = importService.doImport(repoCollection, ADD, PACKAGE_DEFAULT);
		validateImportReport(importReport, ImmutableMap.of(entityId, 10, entityId + "Sample", 10),
				ImmutableSet.of(entityId, entityId + "Sample"));

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
		//	Verify info "AA=G|||;AC=0;AF=0.000199681;AFR_AF=0;AMR_AF=0.0014;AN=6;DP=21572;EAS_AF=0;EUR_AF=0;NS=2504;SAS_AF=0");
		assertEquals(firstRow.getString("AA"), "G|||");
		assertEquals(firstRow.getString("AC"), "0");
		assertEquals(firstRow.getString("AF"), "1.99681E-4");
		assertEquals(firstRow.getString("AFR_AF"), "0.0");
		assertEquals(firstRow.getString("AMR_AF"), "0.0014");
		assertEquals(firstRow.getInt("AN"), Integer.valueOf(6));
		assertEquals(firstRow.getInt("DP"), Integer.valueOf(21572));
		assertEquals(firstRow.getString("EAS_AF"), "0.0");
		assertEquals(firstRow.getString("EUR_AF"), "0.0");
		assertEquals(firstRow.getInt("NS"), Integer.valueOf(2504));
		assertEquals(firstRow.getString("SAS_AF"), "0.0");

		// Verify Samples
		ImmutableList<Entity> samples = ImmutableList.copyOf(firstRow.getEntities(VcfAttributes.SAMPLES).iterator());
		assertEquals(samples.size(), 1);
		Entity firstRowSample = samples.get(0);
		assertEquals(firstRowSample.getIdValue(), firstRow.getIdValue() + "0");
		assertEquals(firstRowSample.getString(VcfAttributes.FORMAT_GT), "0|1");

		Entity lastRow = entities.get(entities.size() - 1);
		assertEquals(lastRow.getString("#CHROM"), "X");
		assertEquals(Integer.valueOf(100640780), lastRow.getInt("POS"));
		assertEquals(lastRow.getString(VcfAttributes.ID), ""); // dot is imported as empty
		assertEquals(lastRow.getString(VcfAttributes.REF), "A");
		assertEquals(lastRow.getString(VcfAttributes.ALT), "T");
		assertEquals(lastRow.getString(VcfAttributes.QUAL), "100");
		assertEquals(lastRow.getString(VcfAttributes.FILTER), "PASS");
		// Verify info "AA=G|||;AC=0;AF=0.000199681;AFR_AF=0;AMR_AF=0.0014;AN=6;DP=21572;EAS_AF=0;EUR_AF=0;NS=2504;SAS_AF=0");
		assertEquals(lastRow.getString("AA"), "G|||");
		assertEquals(lastRow.getString("AC"), "0");
		assertEquals(lastRow.getString("AF"), "1.99681E-4");
		assertEquals(lastRow.getString("AFR_AF"), "0.0");
		assertEquals(lastRow.getString("AMR_AF"), "0.0014");
		assertEquals(lastRow.getInt("AN"), Integer.valueOf(6));
		assertEquals(lastRow.getInt("DP"), Integer.valueOf(21572));
		assertEquals(lastRow.getString("EAS_AF"), "0.0");
		assertEquals(lastRow.getString("EUR_AF"), "0.0");
		assertEquals(lastRow.getInt("NS"), Integer.valueOf(2504));
		assertEquals(lastRow.getString("SAS_AF"), "0.0");

		// Verify Samples
		samples = ImmutableList.copyOf(lastRow.getEntities(VcfAttributes.SAMPLES).iterator());
		assertEquals(samples.size(), 1);
		Entity lastRowSample = samples.get(0);
		assertEquals(lastRowSample.getIdValue(), lastRow.getIdValue() + "0");
		assertEquals(lastRowSample.getString(VcfAttributes.FORMAT_GT), "1|1");
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_READ_PACKAGE, ROLE_READ_ENTITY_TYPE, ROLE_READ_ATTRIBUTE })
	@Test
	public void testDoImportVcfGzWithSamplesAsNonSuperuser()
	{
		testDoImportVcfGzWithSamples();
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void testDoImportVcfGzWithSamplesAsSuperuser()
	{
		testDoImportVcfGzWithSamples();
	}

	private void testDoImportVcfGzWithSamples()
	{
		String entityId = "variantsWithSamplesGz";
		String fileName = entityId + ".vcf.gz";
		File file = getFile("/vcf/" + fileName);
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport importReport = importService.doImport(repoCollection, ADD, PACKAGE_DEFAULT);
		validateImportReport(importReport,
				ImmutableMap.of("variantsWithSamplesGz", 10, "variantsWithSamplesGzSample", 10),
				ImmutableSet.of("variantsWithSamplesGz", "variantsWithSamplesGzSample"));

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
		//	Verify info "AA=G|||;AC=0;AF=0.000199681;AFR_AF=0;AMR_AF=0.0014;AN=6;DP=21572;EAS_AF=0;EUR_AF=0;NS=2504;SAS_AF=0");
		assertEquals(firstRow.getString("AA"), "G|||");
		assertEquals(firstRow.getString("AC"), "0");
		assertEquals(firstRow.getString("AF"), "1.99681E-4");
		assertEquals(firstRow.getString("AFR_AF"), "0.0");
		assertEquals(firstRow.getString("AMR_AF"), "0.0014");
		assertEquals(firstRow.getInt("AN"), Integer.valueOf(6));
		assertEquals(firstRow.getInt("DP"), Integer.valueOf(21572));
		assertEquals(firstRow.getString("EAS_AF"), "0.0");
		assertEquals(firstRow.getString("EUR_AF"), "0.0");
		assertEquals(firstRow.getInt("NS"), Integer.valueOf(2504));
		assertEquals(firstRow.getString("SAS_AF"), "0.0");

		// Verify Samples
		ImmutableList<Entity> samples = ImmutableList.copyOf(firstRow.getEntities(VcfAttributes.SAMPLES).iterator());
		assertEquals(samples.size(), 1);
		Entity firstRowSample = samples.get(0);
		assertEquals(firstRowSample.getIdValue(), firstRow.getIdValue() + "0");
		assertEquals(firstRowSample.getString(VcfAttributes.FORMAT_GT), "0|1");

		Entity lastRow = entities.get(entities.size() - 1);
		assertEquals(lastRow.getString("#CHROM"), "X");
		assertEquals(Integer.valueOf(100640780), lastRow.getInt("POS"));
		assertEquals(lastRow.getString(VcfAttributes.ID), ""); // dot is imported as empty
		assertEquals(lastRow.getString(VcfAttributes.REF), "A");
		assertEquals(lastRow.getString(VcfAttributes.ALT), "T");
		assertEquals(lastRow.getString(VcfAttributes.QUAL), "100");
		assertEquals(lastRow.getString(VcfAttributes.FILTER), "PASS");
		// Verify info "AA=G|||;AC=0;AF=0.000199681;AFR_AF=0;AMR_AF=0.0014;AN=6;DP=21572;EAS_AF=0;EUR_AF=0;NS=2504;SAS_AF=0");
		assertEquals(lastRow.getString("AA"), "G|||");
		assertEquals(lastRow.getString("AC"), "0");
		assertEquals(lastRow.getString("AF"), "1.99681E-4");
		assertEquals(lastRow.getString("AFR_AF"), "0.0");
		assertEquals(lastRow.getString("AMR_AF"), "0.0014");
		assertEquals(lastRow.getInt("AN"), Integer.valueOf(6));
		assertEquals(lastRow.getInt("DP"), Integer.valueOf(21572));
		assertEquals(lastRow.getString("EAS_AF"), "0.0");
		assertEquals(lastRow.getString("EUR_AF"), "0.0");
		assertEquals(lastRow.getInt("NS"), Integer.valueOf(2504));
		assertEquals(lastRow.getString("SAS_AF"), "0.0");

		// Verify Samples
		samples = ImmutableList.copyOf(lastRow.getEntities(VcfAttributes.SAMPLES).iterator());
		assertEquals(samples.size(), 1);
		Entity lastRowSample = samples.get(0);
		assertEquals(lastRowSample.getIdValue(), lastRow.getIdValue() + "0");
		assertEquals(lastRowSample.getString(VcfAttributes.FORMAT_GT), "1|1");
	}

	@DataProvider(name = "doImportEmxAddProvider")
	public Iterator<Object[]> doImportEmxAddProvider()
	{
		List<Object[]> data = new ArrayList<>();

		data.add(createAddData("it_emx_datatypes.xlsx", asList("it", "emx", "datatypes"),
				ImmutableMap.<String, Integer>builder().put("TypeTestRef", 5).put("TypeTest", 38).build(),
				ImmutableSet.of("Person", "TypeTestRef", "Location", "TypeTest"), this::validateItEmxDataTypes));

		data.add(createAddData("it_emx_deep_nesting.xlsx", asList("it", "deep"),
				ImmutableMap.<String, Integer>builder().put("TestCategorical1", 50).put("TestMref1", 50)
						.put("TestXref2", 50).put("TestXref1", 50)
						.put("advanced" + PACKAGE_SEPARATOR + "p" + PACKAGE_SEPARATOR + "TestEntity2", 50).build(),
				ImmutableSet.of("TestCategorical1", "TestMref1", "TestXref2", "TestXref1", "TestEntity0",
						"advanced_TestEntity1", "advanced_p_TestEntity2"), this::validateItEmxDeepNesting));

		data.add(createAddData("it_emx_lookup_attribute.xlsx", asList("it", "emx", "lookupattribute"),
				ImmutableMap.<String, Integer>builder().put("Ref1", 2).put("Ref2", 2).put("Ref3", 2).put("Ref4", 2)
						.put("Ref5", 2).build(), ImmutableSet
						.of("AbstractTop", "AbstractMiddle", "Ref1", "Ref2", "Ref3", "Ref4", "Ref5",
								"TestLookupAttributes"), this::validateItEmxLookupAttribute));

		data.add(createAddData("it_emx_autoid.xlsx", asList("it", "emx", "autoid"), ImmutableMap.of("testAutoId", 4),
				ImmutableSet.of("testAutoId"), this::validateItEmxAutoid));

		data.add(createAddData("it_emx_onetomany.xlsx", asList("it", "emx", "onetomany"),
				ImmutableMap.<String, Integer>builder().put("book", 4).put("author", 2).put("node", 4).build(),
				ImmutableSet.of("book", "author", "node"), this::validateItEmxOneToMany));

		data.add(createAddData("it_emx_self_references.xlsx", asList("it", "emx", "selfreferences"),
				ImmutableMap.of("PersonTest", 11), ImmutableSet.of("PersonTest"), this::validateItEmxSelfReferences));

		data.add(
				createAddData("it_emx_tags.xlsx", asList("it", "emx", "tags"), emptyMap(), ImmutableSet.of("TagEntity"),
						this::validateItEmxTags));

		return data.iterator();
	}

	private Map<String, Object> entityToMap(Entity entity)
	{
		Map<String, Object> entityMap = newHashMap();
		Iterable<Attribute> attributes = entity.getEntityType().getAllAttributes();

		for (Attribute attribute : attributes)
		{
			if (attribute.getDataType().equals(COMPOUND))
			{
				continue;
			}

			String attributeName = attribute.getName();
			Object value = null;
			switch (attribute.getDataType())
			{
				case CATEGORICAL:
				case FILE:
				case XREF:
					if (entity.getEntity(attributeName) != null)
					{
						value = entity.getEntity(attributeName).getIdValue();
					}
					break;
				case CATEGORICAL_MREF:
				case MREF:
				case ONE_TO_MANY:
					value = getIdsAsSet(entity.getEntities(attributeName));
					break;
				default:
					value = entity.get(attributeName);
					break;
			}
			entityMap.put(attributeName, value);
		}

		return entityMap;
	}

	private void validateItEmxDataTypes()
	{
		List<Entity> typeTestEntities = dataService.findAll("it_emx_datatypes_TypeTest").collect(Collectors.toList());
		Map<String, Object> actualTypeTestFirstRow = entityToMap(typeTestEntities.get(0));
		Map<String, Object> actualTypeTestLastRow = entityToMap(getLast(typeTestEntities));

		assertEquals(actualTypeTestFirstRow, typeTestFirstRow);
		assertEquals(actualTypeTestLastRow, typeTestLastRow);

		List<Entity> typeTestRefEntities = dataService.findAll("it_emx_datatypes_TypeTestRef")
				.collect(Collectors.toList());
		Map<String, Object> actualTypeTestRefFirstRow = entityToMap(typeTestRefEntities.get(0));
		Map<String, Object> actualTypeTestRefLastRow = entityToMap(getLast(typeTestRefEntities));

		assertEquals(actualTypeTestRefFirstRow, typeTestRefFirstRow);
		assertEquals(actualTypeTestRefLastRow, typeTestRefLastRow);
	}

	private Set<Object> getIdsAsSet(Iterable<Entity> entities)
	{
		return asStream(entities).map(Entity::getIdValue).collect(toSet());
	}

	private void validateItEmxDeepNesting()
	{

	}

	private void validateItEmxLookupAttribute()
	{

	}

	private void validateItEmxAutoid()
	{

	}

	private void validateItEmxOneToMany()
	{

	}

	private void validateItEmxSelfReferences()
	{

	}

	private void validateItEmxTags()
	{

	}

	@Test(dataProvider = "doImportEmxAddProvider")
	@WithMockUser(username = USERNAME, roles = { ROLE_READ_PACKAGE, ROLE_READ_ENTITY_TYPE, ROLE_READ_ATTRIBUTE,
			ROLE_READ_TAG, ROLE_READ_OWNED, ROLE_READ_FILE_META })
	public void testDoImportAddEmxAsNonSuperuser(File file, Map<String, Integer> entityCountMap,
			Set<String> addedEntityTypes, Runnable entityValidationMethod)
	{
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
			Map<String, Integer> entityCountMap, Set<String> entityTypeNames, Runnable entityValidationMethod)
	{
		File file = getFile("/xls/" + fileName);
		String packageName = String.join(PACKAGE_SEPARATOR, packageTokens);
		Map<String, Object> entityTypeCountMap = entityCountMap.entrySet().stream().collect(
				Collectors.toMap(entry -> packageName + PACKAGE_SEPARATOR + entry.getKey(), Map.Entry::getValue));
		Set<String> entityTypeFullyQualifiedNames = entityTypeNames.stream()
				.map(entityName -> packageName + PACKAGE_SEPARATOR + entityName).collect(toSet());
		return new Object[] { file, entityTypeCountMap, entityTypeFullyQualifiedNames, entityValidationMethod };
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
		typeTestFirstRow.put("xcategoricalmref_value", newHashSet("ref2", "ref1")); //FIXME!!! should be [ref1]
		typeTestFirstRow.put("xcatmrefnillable_value", newHashSet("ref1", "ref3")); //FIXME!!! should be [ref1]
		typeTestFirstRow.put("xdate", Date.from(LocalDate.of(1985, 8, 1).atStartOfDay(UTC).toInstant()));
		typeTestFirstRow.put("xdatenillable", Date.from(LocalDate.of(1985, 8, 1).atStartOfDay(UTC).toInstant()));
		typeTestFirstRow.put("xdatetime", Date.from(Instant.parse("1985-08-12T06:12:13Z")));
		typeTestFirstRow.put("xdatetimenillable", Date.from(Instant.parse("1985-08-12T06:12:13Z")));
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
		typeTestLastRow.put("xcategoricalmref_value", newHashSet("ref1", "ref2")); //FIXME should be [ref1, ref2, ref3]
		typeTestLastRow.put("xcatmrefnillable_value", newHashSet()); //FIXME should be [ref1, ref3]
		typeTestLastRow.put("xdate", Date.from(LocalDate.of(1985, 8, 1).atStartOfDay(UTC).toInstant()));
		typeTestLastRow.put("xdatenillable", Date.from(LocalDate.of(2015, 4, 1).atStartOfDay(UTC).toInstant()));
		typeTestLastRow.put("xdatetime", Date.from(Instant.parse("1985-08-12T06:12:13Z")));
		typeTestLastRow.put("xdatetimenillable", Date.from(Instant.parse("1985-08-12T06:12:13Z")));
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
		typeTestLastRow.put("xlongnillable", 2147483647L); //FIXME should be 12342151234L
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
}
