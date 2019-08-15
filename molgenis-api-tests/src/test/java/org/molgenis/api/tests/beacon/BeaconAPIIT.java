package org.molgenis.api.tests.beacon;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.molgenis.api.tests.utils.RestTestUtils.BAD_REQUEST;
import static org.molgenis.api.tests.utils.RestTestUtils.CREATED;
import static org.molgenis.api.tests.utils.RestTestUtils.OKE;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.READ;
import static org.molgenis.api.tests.utils.RestTestUtils.createUser;
import static org.molgenis.api.tests.utils.RestTestUtils.removeEntities;
import static org.molgenis.api.tests.utils.RestTestUtils.removeEntityFromTable;
import static org.molgenis.api.tests.utils.RestTestUtils.removeRightsForUser;
import static org.molgenis.api.tests.utils.RestTestUtils.setGrantedRepositoryPermissions;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.molgenis.api.tests.AbstractApiTests;
import org.molgenis.api.tests.utils.RestTestUtils;
import org.molgenis.beacon.controller.BeaconController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.collections.Maps;

public class BeaconAPIIT extends AbstractApiTests {
  private static final Logger LOG = LoggerFactory.getLogger(BeaconAPIIT.class);

  // User credentials
  private static final String BEACON_TEST_USER_PASSWORD = "beacon_test_user_password";

  private String adminToken;
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
  private String testUsername;

  /**
   * Pass down system properties via the mvn commandline argument
   *
   * <p>example: mvn test -Dtest="BeaconAPIIT" -DREST_TEST_HOST="https://molgenis01.gcc.rug.nl"
   * -DREST_TEST_ADMIN_NAME="admin" -DREST_TEST_ADMIN_PW="admin"
   */
  @BeforeClass
  public void beforeClass() {
    AbstractApiTests.setUpBeforeClass();
    adminToken = AbstractApiTests.getAdminToken();

    testUsername = "beacon_test_user" + System.currentTimeMillis();
    createUser(adminToken, testUsername, BEACON_TEST_USER_PASSWORD);

    RestTestUtils.uploadVCFToEntity(adminToken, "/beacon_set.vcf", BEACON_SET);

    Map<String, List<Map<String, String>>> beaconOrganisations = Maps.newHashMap();
    Map<String, String> beaconOrganisation = Maps.newHashMap();
    beaconOrganisation.put("id", MY_FIRST_BEACON_ORGANIZATION);
    beaconOrganisation.put("name", "My first beacon organization");
    beaconOrganisations.put("entities", Lists.newArrayList(beaconOrganisation));

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .body(gson.toJson(beaconOrganisations))
        .post("/api/v2/" + SYS_BEACONS_BEACON_ORGANIZATION)
        .then()
        .statusCode(CREATED);

    Map<String, List<Map<String, String>>> beaconDatasets = Maps.newHashMap();
    Map<String, String> beaconDataset = Maps.newHashMap();
    beaconDataset.put("id", MY_FIRST_BEACON_DATASET);
    beaconDataset.put("label", "My first beacon dataset");
    beaconDataset.put("data_set_entity_type", "beacon_set");
    beaconDataset.put("genome_browser_attributes", "VCF");
    beaconDatasets.put("entities", Lists.newArrayList(beaconDataset));

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .body(gson.toJson(beaconDatasets))
        .post("/api/v2/" + SYS_BEACONS_BEACON_DATASET)
        .then()
        .statusCode(CREATED);

    Map<String, List<Map<String, Object>>> beacons = Maps.newHashMap();
    Map<String, Object> beacon = Maps.newHashMap();
    beacon.put("id", MY_FIRST_BEACON);
    beacon.put("name", "My first beacon");
    beacon.put("api_version", "v0.3.0");
    beacon.put("data_sets", Lists.newArrayList(MY_FIRST_BEACON_DATASET));
    beacons.put("entities", Lists.newArrayList(beacon));

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .body(gson.toJson(beacons))
        .post("/api/v2/" + SYS_BEACON)
        .then()
        .statusCode(CREATED);

    setGrantedRepositoryPermissions(
        adminToken,
        testUsername,
        ImmutableMap.<String, Permission>builder()
            .put("sys_md_Package", READ)
            .put("sys_md_EntityType", READ)
            .put("sys_md_Attribute", READ)
            .put("sys_genomebrowser_GenomeBrowserAttributes", READ)
            .put(SYS_BEACON, READ)
            .put(SYS_BEACONS_BEACON_DATASET, READ)
            .put(SYS_BEACONS_BEACON_ORGANIZATION, READ)
            .put(BEACON_SET, READ)
            .build());

    userToken = RestTestUtils.login(testUsername, BEACON_TEST_USER_PASSWORD);
  }

  @Test
  public void testBeaconList() {
    given(userToken)
        .when()
        .get(BeaconController.URI + "/list")
        .then()
        .statusCode(OKE)
        .body("id", hasItem(MY_FIRST_BEACON));
  }

  @Test
  public void testGetBeaconById() {
    given(userToken)
        .when()
        .get(BeaconController.URI + "/" + MY_FIRST_BEACON)
        .then()
        .statusCode(OKE)
        .body("id", equalTo(MY_FIRST_BEACON));
  }

  @Test
  public void testGetQueryBeaconById() {
    given(userToken)
        .when()
        .get(
            BeaconController.URI
                + "/"
                + MY_FIRST_BEACON
                + "/query?referenceName=7&start=130148888&referenceBases=A&alternateBases=C")
        .then()
        .statusCode(OKE)
        .body("exists", equalTo(true));
  }

  @Test
  public void testGetQueryBeaconByIdUnknownBeacon() {
    given(userToken)
        .when()
        .get(
            BeaconController.URI
                + "/MyFirstBeaco/query?referenceName=7&start=130148888&referenceBases=A&alternateBases=C")
        .then()
        .statusCode(BAD_REQUEST)
        .body("error.message", equalTo("Unknown beacon [MyFirstBeaco]"));
  }

  @Test
  public void testPostQueryBeaconById() {
    given(userToken)
        .contentType(APPLICATION_JSON_VALUE)
        .when()
        .body(
            "{\n"
                + "  \"referenceName\": \"7\",\n"
                + "  \"referenceBases\": \"A\",\n"
                + "  \"alternateBases\": \"C\",\n"
                + "  \"start\": 130148888\n"
                + "}")
        .post(BeaconController.URI + "/MyFirstBeacon/query")
        .then()
        .statusCode(OKE)
        .body("beaconId", equalTo(MY_FIRST_BEACON));
  }

  @Test
  public void testPostQueryBeaconByIdUnknownBeacon() {
    given(userToken)
        .contentType(APPLICATION_JSON_VALUE)
        .when()
        .body(
            "{\n"
                + "  \"referenceName\": \"7\",\n"
                + "  \"referenceBases\": \"A\",\n"
                + "  \"alternateBases\": \"C\",\n"
                + "  \"start\": 130148888\n"
                + "}")
        .post(BeaconController.URI + "/MyFirst/query")
        .then()
        .statusCode(BAD_REQUEST)
        .body("error.message", equalTo("Unknown beacon [MyFirst]"));
  }

  @AfterClass(alwaysRun = true)
  public void afterClass() {
    // Cleanup beacon config
    removeEntityFromTable(adminToken, SYS_BEACON, MY_FIRST_BEACON);
    removeEntityFromTable(adminToken, SYS_BEACONS_BEACON_DATASET, MY_FIRST_BEACON_DATASET);
    removeEntityFromTable(
        adminToken, SYS_BEACONS_BEACON_ORGANIZATION, MY_FIRST_BEACON_ORGANIZATION);

    // Cleanup beacon data
    removeEntities(adminToken, Arrays.asList(BEACON_SET, BEACON_SET_SAMPLE));

    // Clean up permissionsr
    removeRightsForUser(adminToken, testUsername);

    AbstractApiTests.tearDownAfterClass();
  }
}
