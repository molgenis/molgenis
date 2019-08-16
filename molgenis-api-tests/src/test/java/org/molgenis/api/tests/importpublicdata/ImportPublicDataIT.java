package org.molgenis.api.tests.importpublicdata;

import static org.springframework.test.util.AssertionErrors.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import com.google.common.base.Strings;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.tests.AbstractApiTests;
import org.molgenis.api.tests.utils.RestTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Regression test that should be run on the build server. */
public class ImportPublicDataIT extends AbstractApiTests {
  private static final Logger LOG = LoggerFactory.getLogger(ImportPublicDataIT.class);

  private String adminToken;
  private List<String> testUrls = Collections.emptyList();
  private String uploadTopLevelPackage;
  private List<String> entitiesToRemove = Collections.emptyList();

  @BeforeClass
  public void beforeClass() {
    AbstractApiTests.setUpBeforeClass();
    adminToken = AbstractApiTests.getAdminToken();

    LOG.info("Importing Test data");

    // Folder to look for test data
    String uploadFolder = getExpectedEnvVariable("MOLGENIS_TEST_UPLOAD_FOLDER");

    // File to import
    String uploadFile = getExpectedEnvVariable("MOLGENIS_TEST_UPLOAD_FILE");

    // Optional package to delete after test
    uploadTopLevelPackage = getOptionalEnvVariable("MOLGENIS_TEST_UPLOAD_PACKAGE_TO_REMOVE");

    // Optional comma separated list of entities to delete after test
    String entitiesToRemoveString =
        getOptionalEnvVariable("MOLGENIS_TEST_UPLOAD_ENTITIES_TO_REMOVE");
    if (!Strings.isNullOrEmpty(entitiesToRemoveString)) {
      entitiesToRemove = Arrays.asList(entitiesToRemoveString.split("\\s*,\\s*"));
    }

    // Comma separated list of urls to test
    String testUrlList = getExpectedEnvVariable("MOLGENIS_TEST_UPLOAD_CHECK_URLS");
    testUrls = Arrays.asList(testUrlList.split("\\s*,\\s*"));

    String importStatus = "PENDING";
    if (uploadFile.endsWith(".xlsx")) {
      importStatus = RestTestUtils.uploadEMXWithoutPackage(adminToken, uploadFolder, uploadFile);
    } else if (uploadFile.endsWith(".vcf")) {
      importStatus = RestTestUtils.uploadVCF(adminToken, uploadFolder, uploadFile);
    } else {
      assertTrue(
          "Import failed, import test template does not support give file type, " + uploadFile,
          false);
    }

    assertEquals("File import failed, file: " + uploadFile, "FINISHED", importStatus);
  }

  private String getExpectedEnvVariable(String envName) {
    String envVariable = System.getenv(envName);
    assertFalse(
        "Expected enviroment variable '"
            + envName
            + "' not found. This test should be run on the build server, are you trying to run it locally?",
        Strings.isNullOrEmpty(envVariable));
    LOG.info("Test upload " + envName + ": " + envVariable);
    return envVariable;
  }

  @Nullable
  @CheckForNull
  private String getOptionalEnvVariable(String envName) {
    String envVariable = System.getenv(envName);
    if (!Strings.isNullOrEmpty(envVariable)) {
      LOG.info("Test upload " + envName + ": " + envVariable);
    }
    return envVariable;
  }

  @Test
  public void testDataWasUploaded() {
    testUrls.forEach(
        testUrl -> given().contentType("text/plain").when().get(testUrl).then().statusCode(200));
  }

  @AfterClass(alwaysRun = true)
  public void afterClass() {
    if (!Strings.isNullOrEmpty(uploadTopLevelPackage)) {
      RestTestUtils.removePackages(adminToken, Collections.singletonList(uploadTopLevelPackage));
    }

    RestTestUtils.removeEntities(adminToken, entitiesToRemove);

    AbstractApiTests.tearDownAfterClass();
  }
}
