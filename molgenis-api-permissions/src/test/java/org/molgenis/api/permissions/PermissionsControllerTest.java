package org.molgenis.api.permissions;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.api.permissions.PermissionsController.BASE_URI;
import static org.molgenis.api.permissions.PermissionsController.DEFAULT_PAGE;
import static org.molgenis.api.permissions.PermissionsController.DEFAULT_PAGESIZE;
import static org.molgenis.api.permissions.PermissionsController.OBJECTS;
import static org.molgenis.api.permissions.PermissionsController.TYPES;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.Sets;
import cz.jirutka.rsql.parser.RSQLParser;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.mockito.Mock;
import org.molgenis.api.permissions.model.request.ObjectPermissionsRequest;
import org.molgenis.api.permissions.model.request.PermissionRequest;
import org.molgenis.api.permissions.model.service.LabelledPermission;
import org.molgenis.api.permissions.rsql.PermissionsQuery;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.security.acl.ObjectIdentityService;
import org.molgenis.web.converter.GsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Sid;
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
public class PermissionsControllerTest extends AbstractMolgenisSpringTest {
  @Autowired private GsonHttpMessageConverter gsonHttpMessageConverter;

  @Mock private PermissionsService permissionsService;
  @Mock private ObjectIdentityService objectIdentityService;
  @Mock private UserRoleTools userRoleTools;
  @Mock private IdentityTools identityTools;
  private MockMvc mockMvc;
  private PrincipalSid user1;
  private PrincipalSid user2;
  private GrantedAuthoritySid role1;
  private GrantedAuthoritySid role2;
  private ObjectIdentityImpl objectIdentity;

  @BeforeMethod
  private void beforeMethod() {
    initMocks(this);
    RSQLParser rsqlParser = new RSQLParser();
    PermissionsController controller =
        new PermissionsController(
            permissionsService, rsqlParser, objectIdentityService, userRoleTools, identityTools);
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setMessageConverters(new FormHttpMessageConverter(), gsonHttpMessageConverter)
            .build();

    user1 = new PrincipalSid("user1");
    user2 = new PrincipalSid("user2");
    role1 = new GrantedAuthoritySid("ROLE_role1");
    role2 = new GrantedAuthoritySid("ROLE_role2");

    objectIdentity = new ObjectIdentityImpl("typeId", "identifier");
  }

  @Test
  public void testCreateAcl() throws Exception {

    when(identityTools.getObjectIdentity("typeId", "identifier")).thenReturn(objectIdentity);
    mockMvc
        .perform(post(BASE_URI + "/" + OBJECTS + "/typeId/identifier"))
        .andExpect(status().isCreated());

    verify(permissionsService).createAcl(objectIdentity);
  }

  @Test
  public void testGetTypes() throws Exception {
    mockMvc.perform(get(BASE_URI + "/" + TYPES)).andExpect(status().isOk());

    verify(permissionsService).getTypes();
  }

  @Test
  public void testGetPermissionsForType() throws Exception {
    mockMvc
        .perform(get(BASE_URI + "/" + TYPES + "/permissions/typeId?q=user==test"))
        .andExpect(status().isOk());

    verify(permissionsService).getSuitablePermissionsForType("typeId");
  }

  @Test
  public void testGetAclsForType() throws Exception {
    when(permissionsService.getAcls("typeId", DEFAULT_PAGE, DEFAULT_PAGESIZE))
        .thenReturn(Arrays.asList("test1", "test2"));
    when(objectIdentityService.getNrOfObjectIdentities("typeId")).thenReturn(80);

    MvcResult result =
        mockMvc
            .perform(get(BASE_URI + "/" + OBJECTS + "/typeId?q=user=in=(test,test2)"))
            .andExpect(status().isOk())
            .andReturn();

    assertEquals(
        result.getResponse().getContentAsString(),
        "{\"page\":{\"size\":100,\"totalElements\":80,\"totalPages\":1,\"number\":1},\"links\":{\"self\":\"http://localhost/api/permissions/objects/typeId?page=1&pageSize=100\"},\"data\":[\"test1\",\"test2\"]}");
  }

  @Test
  public void testGetAcePermission() throws Exception {
    Sid sid1 = new GrantedAuthoritySid("role1");
    Sid sid2 = new GrantedAuthoritySid("role2");
    List<LabelledPermission> objectPermissionResponses =
        Arrays.asList(
            LabelledPermission.create(
                sid1, "typeId", "typeLabel", "identifier", "label", "READ", null),
            LabelledPermission.create(
                sid2, "typeId", "typeLabel", "identifier", "label", "WRITE", null));
    when(permissionsService.getPermission(objectIdentity, Sets.newHashSet(user1, role1), true))
        .thenReturn(objectPermissionResponses);

    PermissionsQuery permissionsQuery = new PermissionsQuery();
    permissionsQuery.setUsers(Collections.singletonList("user1"));
    permissionsQuery.setRoles(Collections.singletonList("role1"));
    when(userRoleTools.getSids(permissionsQuery))
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
    Sid sid1 = new GrantedAuthoritySid("role1");
    Sid sid2 = new GrantedAuthoritySid("role2");
    List<LabelledPermission> objectPermissionResponses =
        Arrays.asList(
            LabelledPermission.create(
                sid1, "typeId", "typeLabel", "identifier", "label", "READ", null),
            LabelledPermission.create(
                sid2, "typeId", "typeLabel", "identifier", "label", "WRITE", null));
    when(permissionsService.getPermissionsForType(
            "typeId", Sets.newHashSet(user1, role1, role2), false))
        .thenReturn(objectPermissionResponses);

    PermissionsQuery permissionsQuery = new PermissionsQuery();
    permissionsQuery.setUsers(Collections.singletonList("user1"));
    permissionsQuery.setRoles(Arrays.asList("role1", "role2"));
    when(userRoleTools.getSids(permissionsQuery))
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
        "{\"links\":{\"self\":\"http://localhost/api/permissions/"
            + "typeId?q=user==user1,role=in=(role1,role2)\"},\"data\":{\"objects\":[{\"objectId\":\"identifier\",\"label\":\"label\",\"permissions\":[{\"user\":\"user1\",\"permission\":\"READ\"},{\"user\":\"role1\",\"permission\":\"WRITE\"}]}]}}");
  }

  @Test
  public void testGetAcePermissionsPaged() throws Exception {
    Sid sid1 = new GrantedAuthoritySid("role1");
    Sid sid2 = new GrantedAuthoritySid("role2");
    List<LabelledPermission> objectPermissionResponses =
        Arrays.asList(
            LabelledPermission.create(
                sid1, "typeId", "typeLabel", "identifier", "label", "READ", null),
            LabelledPermission.create(
                sid2, "typeId", "typeLabel", "identifier", "label", "WRITE", null));
    when(permissionsService.getPagedPermissionsForType(
            "typeId", Sets.newHashSet(user1, user2, role1, role2), 2, 10))
        .thenReturn(objectPermissionResponses);
    when(objectIdentityService.getNrOfObjectIdentities(
            "typeId", Sets.newHashSet(user1, user2, role1, role2)))
        .thenReturn(80);

    PermissionsQuery permissionsQuery = new PermissionsQuery();
    permissionsQuery.setUsers(Collections.singletonList("test"));
    permissionsQuery.setRoles(Arrays.asList("role1", "role2"));
    when(userRoleTools.getSids(permissionsQuery))
        .thenReturn(Arrays.asList(user1, user2, role1, role2));

    MvcResult result =
        mockMvc
            .perform(
                get(BASE_URI + "/typeId?q=user==test,role=in=(role1,role2)&page=2&pageSize=10"))
            .andExpect(status().isOk())
            .andReturn();

    assertEquals(
        result.getResponse().getContentAsString(),
        "{\"page\":{\"size\":10,\"totalElements\":80,\"totalPages\":8,\"number\":2},\"links\":{\"previous\":\"http://localhost/api/permissions/typeId?q=user==test,role=in=(role1,role2)&page=1&pageSize=10\",\"self\":\"http://localhost/api/permissions/typeId?q=user==test,role=in=(role1,role2)&page=2&pageSize=10\",\"next\":\"http://localhost/api/permissions/typeId?q=user==test,role=in=(role1,role2)&page=3&pageSize=10\"},\"data\":{\"objects\":[{\"objectId\":\"identifier\",\"label\":\"label\",\"permissions\":[{\"user\":\"role2\",\"permission\":\"READ\"},{\"user\":\"role1\",\"permission\":\"WRITE\"}]}]}}");
  }

  @Test
  public void testGetAllPermissionsForUser() throws Exception {
    Sid sid1 = new GrantedAuthoritySid("role1");
    Sid sid2 = new GrantedAuthoritySid("role2");
    List<LabelledPermission> objectPermissionResponses =
        Arrays.asList(
            LabelledPermission.create(
                sid1, "typeId", "typeLabel", "identifier", "label", "READ", null),
            LabelledPermission.create(
                sid2, "typeId", "typeLabel", "identifier", "label", "WRITE", null));
    when(permissionsService.getAllPermissions(Sets.newHashSet(user1), false))
        .thenReturn(objectPermissionResponses);
    PermissionsQuery permissionsQuery = new PermissionsQuery();
    permissionsQuery.setUsers(Collections.singletonList("user1"));
    when(userRoleTools.getSids(permissionsQuery))
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
    when(identityTools.getObjectIdentity("typeId", "identifier")).thenReturn(objectIdentity);
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
    verify(permissionsService)
        .updatePermission(Arrays.asList(permissionRequest1, permissionRequest2), objectIdentity);
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

    verify(permissionsService).updatePermissions(Arrays.asList(permission1, permission2), "typeId");
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

    verify(permissionsService).createPermissions(Arrays.asList(permission1, permission2), "typeId");
  }

  @Test
  public void testCreatePermission() throws Exception {
    when(identityTools.getObjectIdentity("typeId", "identifier")).thenReturn(objectIdentity);
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
    verify(permissionsService).createPermission(permissionRequests, objectIdentity);
  }

  @Test
  public void testDeletePermission() throws Exception {
    when(identityTools.getObjectIdentity("typeId", "identifier")).thenReturn(objectIdentity);
    when(userRoleTools.getSid("user1", null)).thenReturn(user1);
    String requestJson = "{" + "user:user1" + "}";
    mockMvc
        .perform(
            MockMvcRequestBuilders.delete(BASE_URI + "/typeId/identifier")
                .contentType(APPLICATION_JSON_UTF8)
                .content(requestJson))
        .andExpect(status().isNoContent());

    verify(permissionsService).deletePermission(user1, objectIdentity);
  }
}
