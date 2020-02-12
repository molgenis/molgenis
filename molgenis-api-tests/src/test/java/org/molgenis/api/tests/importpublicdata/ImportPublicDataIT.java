package org.molgenis.api.tests.importpublicdata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.base.Strings;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.molgenis.api.tests.AbstractApiTests;
import org.molgenis.api.tests.utils.RestTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Regression test that should be run on the build server. */
class ImportPublicDataIT extends AbstractApiTests {
  private static final Logger LOG = LoggerFactory.getLogger(ImportPublicDataIT.class);

  private static String adminToken;
  private static List<String> testUrls = Collections.emptyList();
  private static String uploadTopLevelPackage;
  private static List<String> entitiesToRemove = Collections.emptyList();

  @BeforeAll
  static void beforeClass() {
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
      importStatus = RestTestUtils.uploadEmxFile(adminToken, uploadFolder, uploadFile);
    } else if (uploadFile.endsWith(".vcf")) {
      importStatus = RestTestUtils.uploadVCF(adminToken, uploadFolder, uploadFile);
    } else {
      assertTrue(
          false,
          "Import failed, import test template does not support give file type, " + uploadFile);
    }

    assertEquals("File import failed, file: " + uploadFile, "FINISHED", importStatus);
  }

  private static String getExpectedEnvVariable(String envName) {
    String envVariable = System.getenv(envName);
    assertFalse(
        Strings.isNullOrEmpty(envVariable),
        "Expected enviroment variable '"
            + envName
            + "' not found. This test should be run on the build server, are you trying to run it locally?");
    LOG.info("Test upload " + envName + ": " + envVariable);
    return envVariable;
  }

  @Nullable
  @CheckForNull
  private static String getOptionalEnvVariable(String envName) {
    String envVariable = System.getenv(envName);
    if (!Strings.isNullOrEmpty(envVariable)) {
      LOG.info("Test upload " + envName + ": " + envVariable);
    }
    return envVariable;
  }

  @Test
  void testDataWasUploaded() {
    testUrls.forEach(
        testUrl -> given().contentType("text/plain").when().get(testUrl).then().statusCode(200));
  }

  @AfterAll
  static void afterClass() {
    if (!Strings.isNullOrEmpty(uploadTopLevelPackage)) {
      RestTestUtils.removePackages(adminToken, Collections.singletonList(uploadTopLevelPackage));
    }

    RestTestUtils.removeEntities(adminToken, entitiesToRemove);

    AbstractApiTests.tearDownAfterClass();
  }
}
