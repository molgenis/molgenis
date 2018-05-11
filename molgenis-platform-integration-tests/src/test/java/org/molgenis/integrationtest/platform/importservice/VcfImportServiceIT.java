package org.molgenis.integrationtest.platform.importservice;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.molgenis.data.file.support.FileRepositoryCollection;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.integrationtest.platform.TestPermissionPopulator;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.test.context.support.WithMockUser;
import org.testng.annotations.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singleton;
import static org.molgenis.data.DatabaseAction.ADD;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;
import static org.molgenis.data.meta.UploadPackage.UPLOAD;

public class VcfImportServiceIT extends ImportServiceIT
{
	private static final String USERNAME = "vcf_user";

	@Override
	User getTestUser()
	{
		User user = userFactory.create();
		user.setUsername(USERNAME);
		user.setPassword("password");
		user.setEmail("v@mail.com");
		return user;
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testDoImportVcfWithoutSamplesAsNonSuperuser()
	{
		populateUserPermissions();
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
		String entityTypeId = "variantsWithoutSamples";
		String fileName = entityTypeId + ".vcf";
		File file = getFile("/vcf/" + fileName);
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport importReport = importService.doImport(repoCollection, ADD, UPLOAD);
		validateImportReport(importReport, ImmutableMap.of(entityTypeId, 10), ImmutableSet.of(entityTypeId));

		assertVariants(entityTypeId, false);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testDoImportVcfWithSamplesAsNonSuperuser()
	{
		populateUserPermissions();
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
		String entityTypeId = "variantsWithSamples";
		String fileName = entityTypeId + ".vcf";
		File file = getFile("/vcf/" + fileName);
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport importReport = importService.doImport(repoCollection, ADD, PACKAGE_DEFAULT);
		validateImportReport(importReport, ImmutableMap.of(entityTypeId, 10, entityTypeId + "Sample", 10),
				ImmutableSet.of(entityTypeId, entityTypeId + "Sample"));

		assertVariants(entityTypeId, true);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testDoImportVcfGzWithSamplesAsNonSuperuser()
	{
		populateUserPermissions();
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
		String entityTypeId = "variantsWithSamplesGz";
		String fileName = entityTypeId + ".vcf.gz";
		File file = getFile("/vcf/" + fileName);
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport importReport = importService.doImport(repoCollection, ADD, UPLOAD);
		validateImportReport(importReport,
				ImmutableMap.of("variantsWithSamplesGz", 10, "variantsWithSamplesGzSample", 10),
				ImmutableSet.of("variantsWithSamplesGz", "variantsWithSamplesGzSample"));

		assertVariants(entityTypeId, true);
	}

	private void assertVariants(String entityTypeId, boolean hasSamples)
	{
		String internalId = "TEgTnVmRzifZt9b1fUMXRQ";

		Map<String, Object> expectedFirstRow = new HashMap<>();
		expectedFirstRow.put(VcfAttributes.INTERNAL_ID, internalId);
		expectedFirstRow.put(VcfAttributes.CHROM, "1");
		expectedFirstRow.put(VcfAttributes.POS, 48554748);
		expectedFirstRow.put(VcfAttributes.ID, ""); // dot is imported as empty
		expectedFirstRow.put(VcfAttributes.REF, "T");
		expectedFirstRow.put(VcfAttributes.ALT, "A");
		expectedFirstRow.put(VcfAttributes.QUAL, "100");
		expectedFirstRow.put(VcfAttributes.FILTER, "PASS");
		if (hasSamples)
		{
			expectedFirstRow.put(VcfAttributes.SAMPLES, singleton(internalId + "0"));
		}

		//	Verify info "AA=G|||;AC=0;AF=0.000199681;AFR_AF=0;AMR_AF=0.0014;AN=6;DP=21572;EAS_AF=0;EUR_AF=0;NS=2504;SAS_AF=0");
		expectedFirstRow.put("AA", "G|||");
		expectedFirstRow.put("AC", "0");
		expectedFirstRow.put("AF", "1.99681E-4");
		expectedFirstRow.put("AFR_AF", "0.0");
		expectedFirstRow.put("AMR_AF", "0.0014");
		expectedFirstRow.put("AN", 6);
		expectedFirstRow.put("DP", 21572);
		expectedFirstRow.put("EAS_AF", "0.0");
		expectedFirstRow.put("EUR_AF", "0.0");
		expectedFirstRow.put("NS", 2504);
		expectedFirstRow.put("SAS_AF", "0.0");

		expectedFirstRow.put("CIEND", null);
		expectedFirstRow.put("MSTART", null);
		expectedFirstRow.put("SVLEN", null);
		expectedFirstRow.put("MEINFO", null);
		expectedFirstRow.put("MC", null);
		expectedFirstRow.put("SVTYPE", null);
		expectedFirstRow.put("CIPOS", null);
		expectedFirstRow.put("IMPRECISE", false);
		expectedFirstRow.put("TSD", null);
		expectedFirstRow.put("MEND", null);
		expectedFirstRow.put("MLEN", null);
		expectedFirstRow.put("CS", null);
		expectedFirstRow.put("END", null);

		Map<String, Object> expectedLastRow = new HashMap<>();
		expectedLastRow.put(VcfAttributes.INTERNAL_ID, "pjV6eJp7XTuojxho3uGG8g");
		expectedLastRow.put(VcfAttributes.CHROM, "X");
		expectedLastRow.put(VcfAttributes.POS, 100640780);
		expectedLastRow.put(VcfAttributes.ID, ""); // dot is imported as empty
		expectedLastRow.put(VcfAttributes.REF, "A");
		expectedLastRow.put(VcfAttributes.ALT, "T");
		expectedLastRow.put(VcfAttributes.QUAL, "100");
		expectedLastRow.put(VcfAttributes.FILTER, "PASS");
		if (hasSamples)
		{
			expectedLastRow.put(VcfAttributes.SAMPLES, singleton("pjV6eJp7XTuojxho3uGG8g0"));
		}

		//	Verify info "AA=G|||;AC=0;AF=0.000199681;AFR_AF=0;AMR_AF=0.0014;AN=6;DP=21572;EAS_AF=0;EUR_AF=0;NS=2504;SAS_AF=0");
		expectedLastRow.put("AA", "G|||");
		expectedLastRow.put("AC", "0");
		expectedLastRow.put("AF", "1.99681E-4");
		expectedLastRow.put("AFR_AF", "0.0");
		expectedLastRow.put("AMR_AF", "0.0014");
		expectedLastRow.put("AN", 6);
		expectedLastRow.put("DP", 21572);
		expectedLastRow.put("EAS_AF", "0.0");
		expectedLastRow.put("EUR_AF", "0.0");
		expectedLastRow.put("NS", 2504);
		expectedLastRow.put("SAS_AF", "0.0");

		expectedLastRow.put("CIEND", null);
		expectedLastRow.put("MSTART", null);
		expectedLastRow.put("SVLEN", null);
		expectedLastRow.put("MEINFO", null);
		expectedLastRow.put("MC", null);
		expectedLastRow.put("SVTYPE", null);
		expectedLastRow.put("CIPOS", null);
		expectedLastRow.put("IMPRECISE", false);
		expectedLastRow.put("TSD", null);
		expectedLastRow.put("MEND", null);
		expectedLastRow.put("MLEN", null);
		expectedLastRow.put("CS", null);
		expectedLastRow.put("END", null);

		verifyFirstAndLastRows(entityTypeId, expectedFirstRow, expectedLastRow);
	}

	@Autowired
	private TestPermissionPopulator testPermissionPopulator;

	private void populateUserPermissions()
	{
		Map<ObjectIdentity, PermissionSet> permissionMap = new HashMap<>();
		permissionMap.put(new EntityTypeIdentity("sys_md_Package"), PermissionSet.WRITE);
		permissionMap.put(new EntityTypeIdentity("sys_md_EntityType"), PermissionSet.WRITE);
		permissionMap.put(new EntityTypeIdentity("sys_md_Attribute"), PermissionSet.WRITE);
		permissionMap.put(new EntityTypeIdentity("sys_dec_DecoratorConfiguration"), PermissionSet.READ);
		permissionMap.put(new PackageIdentity(UPLOAD), PermissionSet.WRITEMETA);

		testPermissionPopulator.populate(permissionMap, SecurityUtils.getCurrentUsername());

	}
}
