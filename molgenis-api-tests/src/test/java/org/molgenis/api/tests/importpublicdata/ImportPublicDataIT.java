package org.molgenis.api.tests.importpublicdata;

import com.google.common.base.Strings;
import io.restassured.RestAssured;
import org.junit.Assert;
import org.molgenis.api.tests.utils.RestTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;

import static io.restassured.RestAssured.given;
import static org.molgenis.api.tests.utils.RestTestUtils.*;

public class ImportPublicDataIT
{
	private static final Logger LOG = LoggerFactory.getLogger(ImportPublicDataIT.class);

	private String adminToken;
	private String testUrl;
	private String uploadTopLevelPackage;

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

		String uploadFolder = getExpectedEnvVariable("molgenis.test.upload.folder");
		String uploadFile = getExpectedEnvVariable("molgenis.test.upload.file");
		uploadTopLevelPackage = getExpectedEnvVariable("molgenis.test.upload.package.to.remove");
		testUrl = getExpectedEnvVariable("molgenis.test.upload.check.url");
		RestTestUtils.uploadEMX(adminToken, uploadFolder, uploadFile);

		LOG.info("Importing Done");
	}

	private String getExpectedEnvVariable(String envName)
	{
		String envVariable = System.getenv(envName);
		Assert.assertFalse( "Expected enviroment variable '" + envName + "' not found.",
				Strings.isNullOrEmpty(envVariable));
		LOG.info("Test upload " + envName + ": " + envVariable);
		return envVariable;
	}

	@Test
	public void testDataWasUploaded()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, adminToken)
			   .contentType("text/plain")
			   .when()
			   .get(testUrl)
			   .then()
			   .log()
			   .all()
			   .statusCode(200);
	}

	@AfterClass(alwaysRun = true)
	public void afterClass()
	{
		RestTestUtils.removePackages(adminToken, Collections.singletonList(uploadTopLevelPackage));
	}
}
