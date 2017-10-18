package org.molgenis.integrationtest.platform.importservice;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.molgenis.data.Entity;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.security.model.UserEntity;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.security.test.context.support.WithMockUser;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static org.molgenis.data.DatabaseAction.ADD;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;
import static org.testng.Assert.assertEquals;

public class VcfImportServiceIT extends ImportServiceIT
{
	private static final String USERNAME = "vcf_user";

	@Override
	UserEntity getTestUser()
	{
		UserEntity user = userFactory.create();
		user.setUsername(USERNAME);
		user.setPassword("password");
		user.setEmail("v@mail.com");
		return user;
	}

	@WithMockUser(username = USERNAME)
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

	@WithMockUser(username = USERNAME)
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

	@WithMockUser(username = USERNAME)
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
}
