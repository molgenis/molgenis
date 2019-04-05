package org.molgenis.api.permissions;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.api.permissions.PermissionsApiController.BASE_URI;
import static org.molgenis.api.permissions.PermissionsApiController.DEFAULT_PAGE;
import static org.molgenis.api.permissions.PermissionsApiController.DEFAULT_PAGESIZE;
import static org.molgenis.api.permissions.PermissionsApiController.OBJECTS;
import static org.molgenis.api.permissions.PermissionsApiController.TYPES;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.Sets;
import cz.jirutka.rsql.parser.RSQLParser;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.mockito.Mock;
import org.molgenis.api.permissions.model.request.ObjectPermissionsRequest;
import org.molgenis.api.permissions.model.request.PermissionRequest;
import org.molgenis.api.permissions.model.response.ObjectPermissionsResponse;
import org.molgenis.api.permissions.model.response.PermissionResponse;
import org.molgenis.api.permissions.model.response.TypePermissionsResponse;
import org.molgenis.api.permissions.rsql.PermissionsQuery;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.security.acl.ObjectIdentityService;
import org.molgenis.web.converter.GsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes = {GsonConfig.class})
public class PermissionApiControllerTest extends AbstractMolgenisSpringTest {
  @Autowired private GsonHttpMessageConverter gsonHttpMessageConverter;

  @Mock private PermissionApiService permissionApiService;
  @Mock private ObjectIdentityService objectIdentityService;
  @Mock private SidConversionTools sidConversionTools;
  private MockMvc mockMvc;
  private PrincipalSid user1;
  private PrincipalSid user2;
  private GrantedAuthoritySid role1;
  private GrantedAuthoritySid role2;

  @BeforeMethod
  private void beforeMethod() {
    initMocks(this);
    RSQLParser rsqlParser = new RSQLParser();
    PermissionsApiController controller =
        new PermissionsApiController(
            permissionApiService, rsqlParser, objectIdentityService, sidConversionTools);
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setMessageConverters(new FormHttpMessageConverter(), gsonHttpMessageConverter)
            .build();

    user1 = new PrincipalSid("user1");
    user2 = new PrincipalSid("user2");
    role1 = new GrantedAuthoritySid("ROLE_role1");
    role2 = new GrantedAuthoritySid("ROLE_role2");
  }

  @Test
  public void testCreateAcl() throws Exception {
    mockMvc
        .perform(post(BASE_URI + "/" + OBJECTS + "/typeId/identifier"))
        .andExpect(status().isCreated());

    verify(permissionApiService).createAcl("typeId", "identifier");
  }

  @Test
  public void testGetClasses() throws Exception {
    mockMvc.perform(get(BASE_URI + "/" + TYPES)).andExpect(status().isOk());

    verify(permissionApiService).getClasses();
  }

  @Test
  public void testGetPermissionsForType() throws Exception {
    mockMvc
        .perform(get(BASE_URI + "/" + TYPES + "/permissions/typeId?q=user==test"))
        .andExpect(status().isOk());

    verify(permissionApiService).getSuitablePermissionsForType("typeId");
  }

  @Test
  public void testGetAclsForType() throws Exception {
    when(permissionApiService.getAcls("typeId", DEFAULT_PAGE, DEFAULT_PAGESIZE))
        .thenReturn(Arrays.asList("test1", "test2"));
    when(objectIdentityService.getNrOfObjectIdentities("typeId")).thenReturn(80);

    MvcResult result =
        mockMvc
            .perform(get(BASE_URI + "/" + OBJECTS + "/typeId?q=user=in=(test,test2)"))
            .andExpect(status().isOk())
            .andReturn();

    assertEquals(
        result.getResponse().getContentAsString(),
        "{\"page\":{\"size\":10000,\"totalElements\":80,\"totalPages\":1,\"number\":1},\"links\":{\"self\":\"http://localhost/api/permissions/v1/objects/typeId?page=1&pageSize=10000\"},\"data\":[\"test1\",\"test2\"]}");
  }

  @Test
  public void testGetAcePermission() throws Exception {
    List<PermissionResponse> permissionResponses =
        Arrays.asList(
            PermissionResponse.create(null, "user1", "READ", null),
            PermissionResponse.create(null, "role1", "WRITE", null));
    when(permissionApiService.getPermission(
            "typeId", "identifier", Sets.newHashSet(user1, role1), true))
        .thenReturn(permissionResponses);

    PermissionsQuery permissionsQuery = new PermissionsQuery();
    permissionsQuery.setUsers(Collections.singletonList("user1"));
    permissionsQuery.setRoles(Collections.singletonList("role1"));
    when(sidConversionTools.getSids(permissionsQuery))
        .thenReturn(
            Arrays.asList(new PrincipalSid("user1"), new GrantedAuthoritySid("ROLE_role1")));

    MvcResult result =
        mockMvc
            .perform(
                get(BASE_URI + "/typeId/identifier?q=user==user1,role==role1&inheritance=true"))
            .andExpect(status().isOk())
            .andReturn();

    assertEquals(
        result.getResponse().getContentAsString(),
        "{\"permissions\":[{\"user\":\"user1\",\"permission\":\"READ\"},{\"user\":\"role1\",\"permission\":\"WRITE\"}]}");
  }

  @Test
  public void testGetAcePermissions() throws Exception {
    List<PermissionResponse> permissionResponses =
        Arrays.asList(
            PermissionResponse.create(null, "user1", "READ", null),
            PermissionResponse.create(null, "role1", "WRITE", null));
    Collection<ObjectPermissionsResponse> identityPermission =
        Collections.singletonList(
            ObjectPermissionsResponse.create("identifier", "label", permissionResponses));
    when(permissionApiService.getPermissionsForType(
            "typeId", Sets.newHashSet(user1, role1, role2), false))
        .thenReturn(identityPermission);

    PermissionsQuery permissionsQuery = new PermissionsQuery();
    permissionsQuery.setUsers(Collections.singletonList("user1"));
    permissionsQuery.setRoles(Arrays.asList("role1", "role2"));
    when(sidConversionTools.getSids(permissionsQuery))
        .thenReturn(
            Arrays.asList(
                new PrincipalSid("user1"),
                new GrantedAuthoritySid("ROLE_role1"),
                new GrantedAuthoritySid("ROLE_role2")));

    MvcResult result =
        mockMvc
            .perform(get(BASE_URI + "/typeId?q=user==user1,role=in=(role1,role2)"))
            .andExpect(status().isOk())
            .andReturn();

    assertEquals(
        result.getResponse().getContentAsString(),
        "{\"links\":{\"self\":\"http://localhost/api/permissions/v1/"
            + "typeId?q=user==user1,role=in=(role1,role2)\"},\"data\":{\"objects\":[{\"objectId\":\"identifier\",\"label\":\"label\",\"permissions\":[{\"user\":\"user1\",\"permission\":\"READ\"},{\"user\":\"role1\",\"permission\":\"WRITE\"}]}]}}");
  }

  @Test
  public void testGetAcePermissionsPaged() throws Exception {
    List<PermissionResponse> permissionResponses =
        Arrays.asList(
            PermissionResponse.create(null, "role2", "READ", null),
            PermissionResponse.create(null, "role1", "WRITE", null));
    Collection<ObjectPermissionsResponse> identityPermission =
        Collections.singletonList(
            ObjectPermissionsResponse.create("identifier", "label", permissionResponses));
    when(permissionApiService.getPagedPermissionsForType(
            "typeId", Sets.newHashSet(user1, user2, role1, role2), 2, 10))
        .thenReturn(identityPermission);
    when(objectIdentityService.getNrOfObjectIdentities(
            "typeId", Sets.newHashSet(user1, user2, role1, role2)))
        .thenReturn(80);

    PermissionsQuery permissionsQuery = new PermissionsQuery();
    permissionsQuery.setUsers(Collections.singletonList("test"));
    permissionsQuery.setRoles(Arrays.asList("role1", "role2"));
    when(sidConversionTools.getSids(permissionsQuery))
        .thenReturn(Arrays.asList(user1, user2, role1, role2));

    MvcResult result =
        mockMvc
            .perform(
                get(BASE_URI + "/typeId?q=user==test,role=in=(role1,role2)&page=2&pageSize=10"))
            .andExpect(status().isOk())
            .andReturn();

    assertEquals(
        result.getResponse().getContentAsString(),
        "{\"page\":{\"size\":10,\"totalElements\":80,\"totalPages\":8,\"number\":2},\"links\":{\"previous\":\"http://localhost/api/permissions/v1/typeId?q=user==test,role=in=(role1,role2)&page=1&pageSize=10\",\"self\":\"http://localhost/api/permissions/v1/typeId?q=user==test,role=in=(role1,role2)&page=2&pageSize=10\",\"next\":\"http://localhost/api/permissions/v1/typeId?q=user==test,role=in=(role1,role2)&page=3&pageSize=10\"},\"data\":{\"objects\":[{\"objectId\":\"identifier\",\"label\":\"label\",\"permissions\":[{\"user\":\"role2\",\"permission\":\"READ\"},{\"user\":\"role1\",\"permission\":\"WRITE\"}]}]}}");
  }

  @Test
  public void testGetAllPermissionsForUser() throws Exception {
    List<PermissionResponse> permissionResponses =
        Arrays.asList(
            PermissionResponse.create(null, "user1", "READ", null),
            PermissionResponse.create(null, "user2", "WRITE", null));
    List<ObjectPermissionsResponse> identityPermission =
        Collections.singletonList(
            ObjectPermissionsResponse.create("identifier", "label", permissionResponses));
    List<TypePermissionsResponse> classPermissionResponses =
        Collections.singletonList(
            TypePermissionsResponse.create("typeId", "label", identityPermission));

    when(permissionApiService.getAllPermissions(Sets.newHashSet(user1), false))
        .thenReturn(classPermissionResponses);
    PermissionsQuery permissionsQuery = new PermissionsQuery();
    permissionsQuery.setUsers(Collections.singletonList("user1"));
    when(sidConversionTools.getSids(permissionsQuery))
        .thenReturn(Collections.singletonList(new PrincipalSid("user1")));

    MvcResult result =
        mockMvc
            .perform(get(BASE_URI + "?q=user==user1&includeInheritance=false"))
            .andExpect(status().isOk())
            .andReturn();

    assertEquals(
        result.getResponse().getContentAsString(),
        "{\"types\":[{\"typeId\":\"typeId\",\"label\":\"label\",\"objects\":[{\"objectId\":\"identifier\",\"label\":\"label\",\"permissions\":[{\"user\":\"user1\",\"permission\":\"READ\"},{\"user\":\"user2\",\"permission\":\"WRITE\"}]}]}]}");
  }

  @Test
  public void testUpdatePermission() throws Exception {
    String requestJson =
        "{"
            + "permissions:[{"
            + "permission:READ,"
            + "user:test2"
            + "},{"
            + "permission:WRITE,"
            + "user:test"
            + "}]"
            + "}";
    mockMvc
        .perform(
            patch(BASE_URI + "/typeId/identifier")
                .contentType(APPLICATION_JSON_UTF8)
                .content(requestJson))
        .andExpect(status().isNoContent());

    PermissionRequest permissionRequest1 = PermissionRequest.create(null, "test2", "READ");
    PermissionRequest permissionRequest2 = PermissionRequest.create(null, "test", "WRITE");
    verify(permissionApiService)
        .updatePermission(
            Arrays.asList(permissionRequest1, permissionRequest2), "typeId", "identifier");
  }

  @Test
  public void testUpdatePermissionsForType() throws Exception {
    String requestJson =
        "{"
            + "objects:[{"
            + "objectId:rij1,"
            + "permissions:["
            + "{"
            + "role:IT_VIEWER,"
            + "permission:WRITE"
            + "},"
            + "{"
            + "role:IT_MANAGER,"
            + "permission:READ"
            + "}"
            + "]},"
            + "{"
            + "objectId:rij2,"
            + "permissions:["
            + "{"
            + "role:IT_VIEWER,"
            + "permission:WRITE"
            + "}"
            + "]}"
            + "]"
            + "}";
    mockMvc
        .perform(
            patch(BASE_URI + "/typeId").contentType(APPLICATION_JSON_UTF8).content(requestJson))
        .andExpect(status().isNoContent());

    ObjectPermissionsRequest permission1 =
        ObjectPermissionsRequest.create(
            "rij1",
            Arrays.asList(
                PermissionRequest.create("IT_VIEWER", null, "WRITE"),
                PermissionRequest.create("IT_MANAGER", null, "READ")));
    ObjectPermissionsRequest permission2 =
        ObjectPermissionsRequest.create(
            "rij2",
            Collections.singletonList(PermissionRequest.create("IT_VIEWER", null, "WRITE")));

    verify(permissionApiService)
        .updatePermissions(Arrays.asList(permission1, permission2), "typeId");
  }

  @Test
  public void testCreatePermissionsForType() throws Exception {
    String requestJson =
        "{"
            + "objects:[{"
            + "objectId:rij1,"
            + "permissions:["
            + "{"
            + "role:IT_VIEWER,"
            + "permission:WRITE"
            + "},"
            + "{"
            + "role:IT_MANAGER,"
            + "permission:READ"
            + "}"
            + "]},"
            + "{"
            + "objectId:rij2,"
            + "permissions:["
            + "{"
            + "role:IT_VIEWER,"
            + "permission:WRITE"
            + "}"
            + "]}"
            + "]"
            + "}";
    mockMvc
        .perform(post(BASE_URI + "/typeId").contentType(APPLICATION_JSON_UTF8).content(requestJson))
        .andExpect(status().isCreated());

    ObjectPermissionsRequest permission1 =
        ObjectPermissionsRequest.create(
            "rij1",
            Arrays.asList(
                PermissionRequest.create("IT_VIEWER", null, "WRITE"),
                PermissionRequest.create("IT_MANAGER", null, "READ")));
    ObjectPermissionsRequest permission2 =
        ObjectPermissionsRequest.create(
            "rij2",
            Collections.singletonList(PermissionRequest.create("IT_VIEWER", null, "WRITE")));

    verify(permissionApiService)
        .createPermissions(Arrays.asList(permission1, permission2), "typeId");
  }

  @Test
  public void testCreatePermission() throws Exception {
    String requestJson =
        "{"
            + "permissions:[{"
            + "permission:READ,"
            + "user:test2"
            + "},{"
            + "permission:WRITE,"
            + "user:test"
            + "}]"
            + "}";
    mockMvc
        .perform(
            post(BASE_URI + "/typeId/identifier")
                .contentType(APPLICATION_JSON_UTF8)
                .content(requestJson))
        .andExpect(status().isCreated());

    List<PermissionRequest> permissionRequests =
        Arrays.asList(
            PermissionRequest.create(null, "test2", "READ"),
            PermissionRequest.create(null, "test", "WRITE"));
    verify(permissionApiService).createPermission(permissionRequests, "typeId", "identifier");
  }

  @Test
  public void testDeletePermission() throws Exception {
    when(sidConversionTools.getSid("user1", null)).thenReturn(user1);
    String requestJson = "{" + "user:user1" + "}";
    mockMvc
        .perform(
            MockMvcRequestBuilders.delete(BASE_URI + "/typeId/identifier")
                .contentType(APPLICATION_JSON_UTF8)
                .content(requestJson))
        .andExpect(status().isNoContent());

    verify(permissionApiService).deletePermission(user1, "typeId", "identifier");
  }
}
