package org.molgenis.api.tests.beacon;

import org.molgenis.beacon.controller.BeaconController;
import org.molgenis.oneclickimporter.controller.OneClickImporterController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.util.Strings;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static com.google.common.io.Resources.getResource;
import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.molgenis.api.tests.utils.RestTestUtils.*;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.READ;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class BeaconAPIIT
{
	private static final Logger LOG = LoggerFactory.getLogger(BeaconAPIIT.class);

	// User credentials
	private static final String BEACON_TEST_USER = "beacon_test_user";
	private static final String BEACON_TEST_USER_PASSWORD = "beacon_test_user_password";

	private String adminToken;
	private String testUserId;
	private String userToken;

	/**
	 * Pass down system properties via the mvn commandline argument
	 * <p>
	 * example:
	 * mvn test -Dtest="BeaconAPIIT" -DREST_TEST_HOST="https://molgenis01.gcc.rug.nl" -DREST_TEST_ADMIN_NAME="admin" -DREST_TEST_ADMIN_PW="admin"
	 */
	@BeforeClass
	public void beforeClass() throws URISyntaxException
	{
		LOG.info("Read environment variables");
		String envHost = System.getProperty("REST_TEST_HOST");
		baseURI = Strings.isNullOrEmpty(envHost) ? DEFAULT_HOST : envHost;
		LOG.info("baseURI: " + baseURI);

		String envAdminName = System.getProperty("REST_TEST_ADMIN_NAME");
		String adminUserName = Strings.isNullOrEmpty(envAdminName) ? DEFAULT_ADMIN_NAME : envAdminName;
		LOG.info("adminUserName: " + adminUserName);

		String envAdminPW = System.getProperty("REST_TEST_ADMIN_PW");
		String adminPassword = Strings.isNullOrEmpty(envHost) ? DEFAULT_ADMIN_PW : envAdminPW;
		LOG.info("adminPassword: " + adminPassword);

		adminToken = login(adminUserName, adminPassword);
		createUser(adminToken, BEACON_TEST_USER, BEACON_TEST_USER_PASSWORD);
		testUserId = getUserId(adminToken, BEACON_TEST_USER);

		URL resourceUrl = getResource(BeaconAPIIT.class, "/beacon_set.vcf");
		File file = new File(new URI(resourceUrl.toString()).getPath());

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, adminToken)
			   .multiPart(file)
			   .post(OneClickImporterController.URI + "/upload")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE);

		grantSystemRights(adminToken, testUserId, "sys_md_Package", READ);
		grantSystemRights(adminToken, testUserId, "sys_md_EntityType", READ);
		grantSystemRights(adminToken, testUserId, "sys_md_Attribute", READ);

		grantSystemRights(adminToken, testUserId, "sys_beacons_Beacon", READ);
		grantSystemRights(adminToken, testUserId, "sys_beacons_BeaconOrganization", READ);
		grantSystemRights(adminToken, testUserId, "beacon_set", READ);
		grantSystemRights(adminToken, testUserId, "beacon_setSample", READ);

		userToken = login(BEACON_TEST_USER, BEACON_TEST_USER_PASSWORD);

	}

	@Test
	public void testBeaconList()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, userToken)
			   .when()
			   .get(BeaconController.URI + "/list")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE)
			   .body("id", hasItem("MyFirstBeacon"));
	}

	@Test
	public void testGetBeaconById()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, userToken)
			   .when()
			   .get(BeaconController.URI + "/MyFirstBeacon")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE)
			   .body("id", equalTo("MyFirstBeacon"));
	}

	@Test
	public void testGetQueryBeaconById()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, userToken)
			   .when()
			   .get(BeaconController.URI
					   + "/MyFirstBeacon/query?referenceName=7&start=130148888&referenceBases=A&alternateBases=C")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE)
			   .body("exists", equalTo(true));
	}

	@Test
	public void testGetQueryBeaconByIdUnknownBeacon()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, userToken)
			   .when()
			   .get(BeaconController.URI
					   + "/MyFirstBeaco/query?referenceName=7&start=130148888&referenceBases=A&alternateBases=C")
			   .then()
			   .log()
			   .all()
			   .statusCode(BAD_REQUEST)
			   .body("error.message", equalTo("Unknown beacon [MyFirstBeaco]"));
	}

	@Test
	public void testPostQueryBeaconById()
	{
		given().log()
			   .all()
			   .contentType(APPLICATION_JSON_VALUE)
			   .header(X_MOLGENIS_TOKEN, userToken)
			   .when()
			   .body("{\n" + "  \"referenceName\": \"7\",\n" + "  \"referenceBases\": \"A\",\n"
					   + "  \"alternateBases\": \"C\",\n" + "  \"start\": 130148888\n" + "}")
			   .post(BeaconController.URI + "/MyFirstBeacon/query")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE)
			   .body("beaconId", equalTo("MyFirstBeacon"));

	}

	@Test
	public void testPostQueryBeaconByIdUnknownBeacon()
	{
		given().log()
			   .all()
			   .contentType(APPLICATION_JSON_VALUE)
			   .header(X_MOLGENIS_TOKEN, userToken)
			   .when()
			   .body("{\n" + "  \"referenceName\": \"7\",\n" + "  \"referenceBases\": \"A\",\n"
					   + "  \"alternateBases\": \"C\",\n" + "  \"start\": 130148888\n" + "}")
			   .post(BeaconController.URI + "/MyFirst/query")
			   .then()
			   .log()
			   .all()
			   .statusCode(BAD_REQUEST)
			   .body("error.message", equalTo("Unknown beacon [MyFirst]"));

	}

	@AfterClass(alwaysRun = true)
	public void afterClass()
	{
		// Clean up permissions
		removeRightsForUser(adminToken, testUserId);

		// Clean up user
		cleanupUser(adminToken, testUserId);
	}
}

