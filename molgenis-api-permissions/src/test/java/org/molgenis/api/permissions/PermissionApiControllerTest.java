package org.molgenis.api.permissions;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.api.permissions.PermissionsApiController.ACE_ENDPOINT;
import static org.molgenis.api.permissions.PermissionsApiController.ACL_ENDPOINT;
import static org.molgenis.api.permissions.PermissionsApiController.BASE_URI;
import static org.molgenis.api.permissions.PermissionsApiController.CLASSES_ENDPOINT;
import static org.molgenis.api.permissions.PermissionsApiController.DEFAULT_PAGE;
import static org.molgenis.api.permissions.PermissionsApiController.DEFAULT_PAGESIZE;
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
import org.molgenis.api.permissions.model.request.IdentityPermissionsRequest;
import org.molgenis.api.permissions.model.request.PermissionRequest;
import org.molgenis.api.permissions.model.response.ClassPermissionsResponse;
import org.molgenis.api.permissions.model.response.IdentityPermissionsResponse;
import org.molgenis.api.permissions.model.response.PermissionResponse;
import org.molgenis.security.acl.ObjectIdentityService;
import org.molgenis.web.converter.GsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes = {GsonConfig.class})
public class PermissionApiControllerTest extends AbstractTestNGSpringContextTests {
  @Autowired private GsonHttpMessageConverter gsonHttpMessageConverter;

  @Mock private PermissionApiService permissionApiService;
  @Mock private ObjectIdentityService objectIdentityService;
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
        new PermissionsApiController(permissionApiService, rsqlParser, objectIdentityService);
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
        .perform(post(BASE_URI + "/" + ACL_ENDPOINT + "/typeId/identifier"))
        .andExpect(status().isCreated());

    verify(permissionApiService).createAcl("typeId", "identifier");
  }

  @Test
  public void testGetClasses() throws Exception {
    mockMvc.perform(get(BASE_URI + "/" + CLASSES_ENDPOINT)).andExpect(status().isOk());

    verify(permissionApiService).getClasses();
  }

  @Test
  public void testGetPermissionsForType() throws Exception {
    mockMvc
        .perform(get(BASE_URI + "/" + CLASSES_ENDPOINT + "/permissions/typeId?q=user==test"))
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
            .perform(get(BASE_URI + "/" + ACL_ENDPOINT + "/typeId?q=user=in=(test,test2)"))
            .andExpect(status().isOk())
            .andReturn();

    assertEquals(
        result.getResponse().getContentAsString(),
        "{\"page\":{\"size\":10000,\"totalElements\":80,\"totalPages\":1,\"number\":1},\"links\":{\"self\":\"http://localhost/api/permissions/v1/acls/typeId?page=1&pageSize=10000\"},\"data\":[\"test1\",\"test2\"]}");
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

    MvcResult result =
        mockMvc
            .perform(
                get(
                    BASE_URI
                        + "/"
                        + ACE_ENDPOINT
                        + "/typeId/identifier?q=user==user1,role==role1&inheritance=true"))
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
    Collection<IdentityPermissionsResponse> identityPermission =
        Collections.singletonList(
            IdentityPermissionsResponse.create("identifier", "label", permissionResponses));
    when(permissionApiService.getPermissionsForType(
            "typeId", Sets.newHashSet(user1, role1, role2), false))
        .thenReturn(identityPermission);

    MvcResult result =
        mockMvc
            .perform(
                get(BASE_URI + "/" + ACE_ENDPOINT + "/typeId?q=user==user1,role=in=(role1,role2)"))
            .andExpect(status().isOk())
            .andReturn();

    assertEquals(
        result.getResponse().getContentAsString(),
        "{\"links\":{\"self\":\"http://localhost/api/permissions/v1/aces/typeId?q=user==user1,role=in=(role1,role2)\"},\"data\":{\"identityPermissions\":[{\"identifier\":\"identifier\",\"label\":\"label\",\"permissions\":[{\"user\":\"user1\",\"permission\":\"READ\"},{\"user\":\"role1\",\"permission\":\"WRITE\"}]}]}}");
  }

  @Test
  public void testGetAcePermissionsPaged() throws Exception {
    List<PermissionResponse> permissionResponses =
        Arrays.asList(
            PermissionResponse.create(null, "role2", "READ", null),
            PermissionResponse.create(null, "role1", "WRITE", null));
    Collection<IdentityPermissionsResponse> identityPermission =
        Collections.singletonList(
            IdentityPermissionsResponse.create("identifier", "label", permissionResponses));
    when(permissionApiService.getPagedPermissionsForType(
            "typeId", Sets.newHashSet(user1, user2, role1, role2), 2, 10))
        .thenReturn(identityPermission);
    when(objectIdentityService.getNrOfObjectIdentities(
            "typeId", Sets.newHashSet(user1, user2, role1, role2)))
        .thenReturn(80);

    MvcResult result =
        mockMvc
            .perform(
                get(
                    BASE_URI
                        + "/"
                        + ACE_ENDPOINT
                        + "/typeId?q=user==test,role=in=(role1,role2)&page=2&pageSize=10"))
            .andExpect(status().isOk())
            .andReturn();

    assertEquals(
        result.getResponse().getContentAsString(),
        "{\"page\":{\"size\":10,\"totalElements\":0,\"totalPages\":0,\"number\":2},\"links\":{\"previous\":\"http://localhost/api/permissions/v1/aces/typeId?q=user==test,role=in=(role1,role2)&page=1&pageSize=10\",\"self\":\"http://localhost/api/permissions/v1/aces/typeId?q=user==test,role=in=(role1,role2)&page=2&pageSize=10\"},\"data\":{\"identityPermissions\":[]}}");
  }

  @Test
  public void testGetAllPermissionsForUser() throws Exception {
    List<PermissionResponse> permissionResponses =
        Arrays.asList(
            PermissionResponse.create(null, "user1", "READ", null),
            PermissionResponse.create(null, "user2", "WRITE", null));
    List<IdentityPermissionsResponse> identityPermission =
        Collections.singletonList(
            IdentityPermissionsResponse.create("identifier", "label", permissionResponses));
    List<ClassPermissionsResponse> classPermissionResponses =
        Collections.singletonList(
            ClassPermissionsResponse.create("classId", "label", identityPermission));

    when(permissionApiService.getAllPermissions(Sets.newHashSet(user1), false))
        .thenReturn(classPermissionResponses);

    MvcResult result =
        mockMvc
            .perform(get(BASE_URI + "/" + ACE_ENDPOINT + "?q=user==user1&includeInheritance=false"))
            .andExpect(status().isOk())
            .andReturn();

    assertEquals(
        result.getResponse().getContentAsString(),
        "{\"classPermissions\":[{\"classId\":\"classId\",\"label\":\"label\",\"rowPermissions\":[{\"identifier\":\"identifier\",\"label\":\"label\",\"permissions\":[{\"user\":\"user1\",\"permission\":\"READ\"},{\"user\":\"user2\",\"permission\":\"WRITE\"}]}]}]}");
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
            patch(BASE_URI + "/" + ACE_ENDPOINT + "/typeId/identifier")
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
            + "rows:[{"
            + "identifier:rij1,"
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
            + "identifier:rij2,"
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
            patch(BASE_URI + "/" + ACE_ENDPOINT + "/typeId")
                .contentType(APPLICATION_JSON_UTF8)
                .content(requestJson))
        .andExpect(status().isNoContent());

    IdentityPermissionsRequest permission1 =
        IdentityPermissionsRequest.create(
            "rij1",
            Arrays.asList(
                PermissionRequest.create("IT_VIEWER", null, "WRITE"),
                PermissionRequest.create("IT_MANAGER", null, "READ")));
    IdentityPermissionsRequest permission2 =
        IdentityPermissionsRequest.create(
            "rij2",
            Collections.singletonList(PermissionRequest.create("IT_VIEWER", null, "WRITE")));

    verify(permissionApiService)
        .updatePermissions(Arrays.asList(permission1, permission2), "typeId");
  }

  @Test
  public void testCreatePermissionsForType() throws Exception {
    String requestJson =
        "{"
            + "rows:[{"
            + "identifier:rij1,"
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
            + "identifier:rij2,"
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
            post(BASE_URI + "/" + ACE_ENDPOINT + "/typeId")
                .contentType(APPLICATION_JSON_UTF8)
                .content(requestJson))
        .andExpect(status().isCreated());

    IdentityPermissionsRequest permission1 =
        IdentityPermissionsRequest.create(
            "rij1",
            Arrays.asList(
                PermissionRequest.create("IT_VIEWER", null, "WRITE"),
                PermissionRequest.create("IT_MANAGER", null, "READ")));
    IdentityPermissionsRequest permission2 =
        IdentityPermissionsRequest.create(
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
            post(BASE_URI + "/" + ACE_ENDPOINT + "/typeId/identifier")
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
    String requestJson = "{" + "user:user1" + "}";
    mockMvc
        .perform(
            MockMvcRequestBuilders.delete(BASE_URI + "/" + ACE_ENDPOINT + "/typeId/identifier")
                .contentType(APPLICATION_JSON_UTF8)
                .content(requestJson))
        .andExpect(status().isNoContent());

    verify(permissionApiService).deletePermission(user1, "typeId", "identifier");
  }
}
