package org.molgenis.api.tests.beacon;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.molgenis.api.tests.utils.RestTestUtils;
import org.molgenis.beacon.controller.BeaconController;
import org.molgenis.data.rest.v2.RestControllerV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.collections.Maps;
import org.testng.util.Strings;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

	private Gson gson = new Gson();

	private static final String SYS_BEACONS_BEACON_ORGANIZATION = "sys_beacons_BeaconOrganization";
	private static final String SYS_BEACONS_BEACON_DATASET = "sys_beacons_BeaconDataset";
	private static final String SYS_BEACON = "sys_beacons_Beacon";

	private static final String MY_FIRST_BEACON_ORGANIZATION = "MyFirstBeaconOrganization";
	private static final String MY_FIRST_BEACON_DATASET = "MyFirstBeaconDataset";
	private static final String MY_FIRST_BEACON = "MyFirstBeacon";

	private static final String BEACON_SET = "beacon_set";
	private static final String BEACON_SET_SAMPLE = "beacon_setSample";

	/**
	 * Pass down system properties via the mvn commandline argument
	 * <p>
	 * example:
	 * mvn test -Dtest="BeaconAPIIT" -DREST_TEST_HOST="https://molgenis01.gcc.rug.nl" -DREST_TEST_ADMIN_NAME="admin" -DREST_TEST_ADMIN_PW="admin"
	 */
	@BeforeClass
	public void beforeClass()
	{
		LOG.info("Read environment variables");
		String envHost = System.getProperty("REST_TEST_HOST");
		baseURI = Strings.isNullOrEmpty(envHost) ? DEFAULT_HOST : envHost;
		LOG.info("baseURI: " + baseURI);

		String envAdminName = System.getProperty("REST_TEST_ADMIN_NAME");
		String adminUserName = Strings.isNullOrEmpty(envAdminName) ? DEFAULT_ADMIN_NAME : envAdminName;
		LOG.info("adminUserName: " + adminUserName);

		String envAdminPW = System.getProperty("REST_TEST_ADMIN_PW");
		String adminPassword = Strings.isNullOrEmpty(envAdminPW) ? DEFAULT_ADMIN_PW : envAdminPW;
		LOG.info("adminPassword: " + adminPassword);

		adminToken = login(adminUserName, adminPassword);
		createUser(adminToken, BEACON_TEST_USER, BEACON_TEST_USER_PASSWORD);
		testUserId = getUserId(adminToken, BEACON_TEST_USER);

		RestTestUtils.uploadVCFToEntity(adminToken, "/beacon_set.vcf", BEACON_SET);

		Map<String, List<Map<String, String>>> beaconOrganisations = Maps.newHashMap();
		Map<String, String> beaconOrganisation = Maps.newHashMap();
		beaconOrganisation.put("id", MY_FIRST_BEACON_ORGANIZATION);
		beaconOrganisation.put("name", "My first beacon organization");
		beaconOrganisations.put("entities", Lists.newArrayList(beaconOrganisation));

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, adminToken)
			   .contentType(APPLICATION_JSON_VALUE)
			   .body(gson.toJson(beaconOrganisations))
			   .post(RestControllerV2.BASE_URI + "/" + SYS_BEACONS_BEACON_ORGANIZATION)
			   .then()
			   .log()
			   .all()
			   .statusCode(CREATED);

		Map<String, List<Map<String, String>>> beaconDatasets = Maps.newHashMap();
		Map<String, String> beaconDataset = Maps.newHashMap();
		beaconDataset.put("id", MY_FIRST_BEACON_DATASET);
		beaconDataset.put("label", "My first beacon dataset");
		beaconDataset.put("data_set_entity_type", "beacon_set");
		beaconDataset.put("genome_browser_attributes", "VCF");
		beaconDatasets.put("entities", Lists.newArrayList(beaconDataset));

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, adminToken)
			   .contentType(APPLICATION_JSON_VALUE)
			   .body(gson.toJson(beaconDatasets))
			   .post(RestControllerV2.BASE_URI + "/" + SYS_BEACONS_BEACON_DATASET)
			   .then()
			   .log()
			   .all()
			   .statusCode(CREATED);

		Map<String, List<Map<String, Object>>> beacons = Maps.newHashMap();
		Map<String, Object> beacon = Maps.newHashMap();
		beacon.put("id", MY_FIRST_BEACON);
		beacon.put("name", "My first beacon");
		beacon.put("api_version", "v0.3.0");
		beacon.put("data_sets", Lists.newArrayList(MY_FIRST_BEACON_DATASET));
		beacons.put("entities", Lists.newArrayList(beacon));

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, adminToken)
			   .contentType(APPLICATION_JSON_VALUE)
			   .body(gson.toJson(beacons))
			   .post(RestControllerV2.BASE_URI + "/" + SYS_BEACON)
			   .then()
			   .log()
			   .all()
			   .statusCode(CREATED);

		grantSystemRights(adminToken, testUserId, "sys_md_Package", READ);
		grantSystemRights(adminToken, testUserId, "sys_md_EntityType", READ);
		grantSystemRights(adminToken, testUserId, "sys_md_Attribute", READ);
		grantSystemRights(adminToken, testUserId, "sys_genomebrowser_GenomeBrowserAttributes", READ);

		grantSystemRights(adminToken, testUserId, SYS_BEACON, READ);
		grantSystemRights(adminToken, testUserId, SYS_BEACONS_BEACON_DATASET, READ);
		grantSystemRights(adminToken, testUserId, SYS_BEACONS_BEACON_ORGANIZATION, READ);
		grantSystemRights(adminToken, testUserId, BEACON_SET, READ);

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
			   .body("id", hasItem(MY_FIRST_BEACON));
	}

	@Test
	public void testGetBeaconById()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, userToken)
			   .when()
			   .get(BeaconController.URI + "/" + MY_FIRST_BEACON)
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE)
			   .body("id", equalTo(MY_FIRST_BEACON));
	}

	@Test
	public void testGetQueryBeaconById()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, userToken)
			   .when()
			   .get(BeaconController.URI + "/" + MY_FIRST_BEACON
					   + "/query?referenceName=7&start=130148888&referenceBases=A&alternateBases=C")
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
			   .body("beaconId", equalTo(MY_FIRST_BEACON));

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
		// Cleanup beacon data
		removeEntities(adminToken, Arrays.asList(BEACON_SET, BEACON_SET_SAMPLE));

		// Cleanup beacon config
		removeEntityFromTable(adminToken, SYS_BEACON, MY_FIRST_BEACON);
		removeEntityFromTable(adminToken, SYS_BEACONS_BEACON_DATASET, MY_FIRST_BEACON_DATASET);
		removeEntityFromTable(adminToken, SYS_BEACONS_BEACON_ORGANIZATION, MY_FIRST_BEACON_ORGANIZATION);

		// Clean up permissions
		removeRightsForUser(adminToken, testUserId);
		// Clean up user
		cleanupUser(adminToken, testUserId);
	}

}

