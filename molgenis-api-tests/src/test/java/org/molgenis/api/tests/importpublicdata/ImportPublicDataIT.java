package org.molgenis.api.tests.importpublicdata;

import com.google.common.base.Strings;
import io.restassured.RestAssured;
import org.molgenis.api.tests.utils.RestTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.molgenis.api.tests.utils.RestTestUtils.*;
import static org.springframework.test.util.AssertionErrors.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

public class ImportPublicDataIT
{
	private static final Logger LOG = LoggerFactory.getLogger(ImportPublicDataIT.class);

	private String adminToken;
	private List<String> testUrls = Collections.emptyList();
	private String uploadTopLevelPackage;
	private List<String> entitiesToRemove = Collections.emptyList();

	@BeforeClass
	public void beforeClass()
	{
		LOG.info("Read environment variables");
		String envHost = System.getProperty("REST_TEST_HOST");
		RestAssured.baseURI = Strings.isNullOrEmpty(envHost) ? RestTestUtils.DEFAULT_HOST : envHost;
		LOG.info("baseURI: " + RestAssured.baseURI);

		String envAdminName = System.getProperty("REST_TEST_ADMIN_NAME");
		String adminUserName = Strings.isNullOrEmpty(envAdminName) ? RestTestUtils.DEFAULT_ADMIN_NAME : envAdminName;
		LOG.info("adminUserName: " + adminUserName);

		String envAdminPW = System.getProperty("REST_TEST_ADMIN_PW");
		String adminPassword = Strings.isNullOrEmpty(envAdminPW) ? RestTestUtils.DEFAULT_ADMIN_PW : envAdminPW;

		adminToken = login(adminUserName, adminPassword);

		LOG.info("Importing Test data");

		// Folder to look for test data
		String uploadFolder = getExpectedEnvVariable("molgenis.test.upload.folder");

		// File to import
		String uploadFile = getExpectedEnvVariable("molgenis.test.upload.file");

		// Optional package to delete after test
		uploadTopLevelPackage = getOptionalEnvVariable("molgenis.test.upload.package.to.remove");

		// Optional comma separated list of entities to delete after test
		String entitiesToRemoveString = getOptionalEnvVariable("molgenis.test.upload.entities.to.remove");
		if (!Strings.isNullOrEmpty(entitiesToRemoveString))
		{
			entitiesToRemove = Arrays.asList(entitiesToRemoveString.split("\\s*,\\s*"));
		}

		// Comma separated list of urls to test
		String testUrlList = getExpectedEnvVariable("molgenis.test.upload.check.urls");
		testUrls = Arrays.asList(testUrlList.split("\\s*,\\s*"));

		String importStatus = "PENDING";
		if(uploadFile.endsWith(".xlsx"))
		{
			importStatus = RestTestUtils.uploadEMX(adminToken, uploadFolder, uploadFile);
		} else if(uploadFile.endsWith(".vcf")){
			importStatus = RestTestUtils.uploadVCF(adminToken, uploadFolder, uploadFile);
		} else {
			assertTrue("Import failed, import test template does not support give file type, " + uploadFile, false);
		}

		assertEquals("File import failed, file: "+ uploadFile,"FINISHED", importStatus);
	}

	private String getExpectedEnvVariable(String envName)
	{
		String envVariable = System.getenv(envName);
		assertFalse("Expected enviroment variable '" + envName + "' not found.",
				Strings.isNullOrEmpty(envVariable));
		LOG.info("Test upload " + envName + ": " + envVariable);
		return envVariable;
	}

	@Nullable
	private String getOptionalEnvVariable(String envName)
	{
		String envVariable = System.getenv(envName);
		if (!Strings.isNullOrEmpty(envVariable))
		{
			LOG.info("Test upload " + envName + ": " + envVariable);
		}
		return envVariable;
	}

	@Test
	public void testDataWasUploaded()
	{
		testUrls.forEach(testUrl -> given().log()
										   .all()
										   .header(X_MOLGENIS_TOKEN, adminToken)
										   .contentType("text/plain")
										   .when()
										   .get(testUrl)
										   .then()
										   .log()
										   .all()
										   .statusCode(200));

	}

	@AfterClass(alwaysRun = true)
	public void afterClass()
	{
		if (!Strings.isNullOrEmpty(uploadTopLevelPackage))
		{
			RestTestUtils.removePackages(adminToken, Collections.singletonList(uploadTopLevelPackage));
		}

		RestTestUtils.removeEntities(adminToken, entitiesToRemove);

	}
}
