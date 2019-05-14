package org.molgenis.api.permissions;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.api.permissions.PermissionsController.BASE_URI;
import static org.molgenis.api.permissions.PermissionsController.DEFAULT_PAGE;
import static org.molgenis.api.permissions.PermissionsController.DEFAULT_PAGESIZE;
import static org.molgenis.api.permissions.PermissionsController.OBJECTS;
import static org.molgenis.api.permissions.PermissionsController.TYPES;
import static org.molgenis.security.core.PermissionSet.READ;
import static org.molgenis.security.core.PermissionSet.WRITE;
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
import java.util.Map;
import java.util.Set;
import org.mockito.Mock;
import org.molgenis.api.permissions.rsql.PermissionsQuery;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.security.permission.EntityHelper;
import org.molgenis.data.security.permission.PermissionService;
import org.molgenis.data.security.permission.UserRoleTools;
import org.molgenis.data.security.permission.model.LabelledObject;
import org.molgenis.data.security.permission.model.LabelledObjectIdentity;
import org.molgenis.data.security.permission.model.LabelledPermission;
import org.molgenis.data.security.permission.model.Permission;
import org.molgenis.security.acl.ObjectIdentityService;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.web.converter.GsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.ObjectIdentity;
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

  @Mock private PermissionService permissionsService;
  @Mock private ObjectIdentityService objectIdentityService;
  @Mock private UserRoleTools userRoleTools;
  @Mock private EntityHelper entityHelper;
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
            permissionsService, rsqlParser, objectIdentityService, userRoleTools, entityHelper);
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
    when(entityHelper.getObjectIdentity("typeId", "identifier")).thenReturn(objectIdentity);
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
    when(permissionsService.getObjects("typeId", DEFAULT_PAGE, DEFAULT_PAGESIZE))
        .thenReturn(
            Sets.newHashSet(
                LabelledObject.create("test1", "label1"),
                LabelledObject.create("test2", "label2")));
    when(objectIdentityService.getNrOfObjectIdentities("typeId")).thenReturn(80);

    MvcResult result =
        mockMvc
            .perform(get(BASE_URI + "/" + OBJECTS + "/typeId?q=user=in=(test,test2)"))
            .andExpect(status().isOk())
            .andReturn();

    assertEquals(
        result.getResponse().getContentAsString(),
        "{\"page\":{\"size\":100,\"totalElements\":80,\"totalPages\":1,\"number\":1},\"links\":{\"self\":\"http://localhost/api/permissions/objects/typeId?page=1&pageSize=100\"},\"data\":[{\"id\":\"test2\",\"label\":\"label2\"},{\"id\":\"test1\",\"label\":\"label1\"}]}");
  }

  @Test
  public void testGetAcePermission() throws Exception {
    Sid sid1 = new GrantedAuthoritySid("ROLE_role1");
    Sid sid2 = new GrantedAuthoritySid("ROLE_role2");
    Set<LabelledPermission> objectPermissionResponses =
        Sets.newHashSet(
            LabelledPermission.create(
                sid1,
                LabelledObjectIdentity.create(
                    "typeId", "entityTypeId", "typeLabel", "identifier", "label"),
                PermissionSet.READ,
                null),
            LabelledPermission.create(
                sid2,
                LabelledObjectIdentity.create(
                    "typeId", "entityTypeId", "typeLabel", "identifier", "label"),
                WRITE,
                null));
    when(permissionsService.getPermissionsForObject(
            objectIdentity, Sets.newHashSet(user1, role1), true))
        .thenReturn(objectPermissionResponses);
    when(userRoleTools.getSids(
            Collections.singletonList("user1"), Collections.singletonList("role1")))
        .thenReturn(Arrays.asList(user1, role1));
    PermissionsQuery permissionsQuery = new PermissionsQuery();
    permissionsQuery.setUsers(Collections.singletonList("user1"));
    permissionsQuery.setRoles(Collections.singletonList("role1"));

    when(entityHelper.getObjectIdentity("typeId", "identifier")).thenReturn(objectIdentity);

    MvcResult result =
        mockMvc
            .perform(
                get(BASE_URI + "/typeId/identifier?q=user==user1,role==role1&inheritance=true"))
            .andExpect(status().isOk())
            .andReturn();

    assertEquals(
        result.getResponse().getContentAsString(),
        "{\"data\":{\"id\":\"identifier\",\"label\":\"label\",\"permissions\":[{\"role\":\"role1\",\"permission\":\"READ\"},{\"role\":\"role2\",\"permission\":\"WRITE\"}]}}");
  }

  @Test
  public void testGetAcePermissions() throws Exception {
    Sid sid1 = new GrantedAuthoritySid("ROLE_role1");
    Sid sid2 = new GrantedAuthoritySid("ROLE_role2");
    Set<LabelledPermission> objectPermissionResponses =
        Sets.newHashSet(
            LabelledPermission.create(
                sid1,
                LabelledObjectIdentity.create(
                    "typeId", "entityTypeId", "typeLabel", "identifier", "label"),
                PermissionSet.READ,
                null),
            LabelledPermission.create(
                sid2,
                LabelledObjectIdentity.create(
                    "typeId", "entityTypeId", "typeLabel", "identifier", "label"),
                WRITE,
                null));
    Map<String, Set<LabelledPermission>> expected =
        Collections.singletonMap("typeId", objectPermissionResponses);
    when(permissionsService.getPermissionsForType(
            "typeId", Sets.newHashSet(user1, role1, role2), false))
        .thenReturn(expected);
    when(userRoleTools.getSids(Collections.singletonList("user1"), Arrays.asList("role1", "role2")))
        .thenReturn(Arrays.asList(user1, role1, role2));
    PermissionsQuery permissionsQuery = new PermissionsQuery();
    permissionsQuery.setUsers(Collections.singletonList("user1"));
    permissionsQuery.setRoles(Arrays.asList("role1", "role2"));

    MvcResult result =
        mockMvc
            .perform(get(BASE_URI + "/typeId?q=user==user1,role=in=(role1,role2)"))
            .andExpect(status().isOk())
            .andReturn();

    assertEquals(
        result.getResponse().getContentAsString(),
        "{\"links\":{\"self\":\"http://localhost/api/permissions/typeId?q=user==user1,role=in=(role1,role2)\"},\"data\":{\"id\":\"typeId\",\"label\":\"typeLabel\",\"objects\":[{\"id\":\"typeId\",\"label\":\"label\",\"permissions\":[{\"role\":\"role1\",\"permission\":\"READ\"},{\"role\":\"role2\",\"permission\":\"WRITE\"}]}]}}");
  }

  @Test
  public void testGetAcePermissionsPaged() throws Exception {
    Sid sid1 = new GrantedAuthoritySid("ROLE_role1");
    Sid sid2 = new GrantedAuthoritySid("ROLE_role2");
    Set<LabelledPermission> objectPermissionResponses =
        Sets.newHashSet(
            LabelledPermission.create(
                sid1,
                LabelledObjectIdentity.create(
                    "typeId", "entityTypeId", "typeLabel", "identifier", "label"),
                PermissionSet.READ,
                null),
            LabelledPermission.create(
                sid2,
                LabelledObjectIdentity.create(
                    "typeId", "entityTypeId", "typeLabel", "identifier", "label"),
                WRITE,
                null));
    Map<String, Set<LabelledPermission>> expected =
        Collections.singletonMap("typeId", objectPermissionResponses);
    when(userRoleTools.getSids(Collections.singletonList("user1"), Arrays.asList("role1", "role2")))
        .thenReturn(Arrays.asList(user1, user2, role1, role2));
    when(permissionsService.getPermissionsForType(
            "typeId", Sets.newHashSet(user1, user2, role1, role2), 2, 10))
        .thenReturn(expected);
    when(objectIdentityService.getNrOfObjectIdentities(
            "typeId", Sets.newHashSet(user1, user2, role1, role2)))
        .thenReturn(80);

    PermissionsQuery permissionsQuery = new PermissionsQuery();
    permissionsQuery.setUsers(Collections.singletonList("user1"));
    permissionsQuery.setRoles(Arrays.asList("role1", "role2"));

    MvcResult result =
        mockMvc
            .perform(
                get(BASE_URI + "/typeId?q=user==user1,role=in=(role1,role2)&page=2&pageSize=10"))
            .andExpect(status().isOk())
            .andReturn();

    assertEquals(
        result.getResponse().getContentAsString(),
        "{\"page\":{\"size\":10,\"totalElements\":80,\"totalPages\":8,\"number\":2},\"links\":{\"previous\":\"http://localhost/api/permissions/typeId?q=user==user1,role=in=(role1,role2)&page=1&pageSize=10\",\"self\":\"http://localhost/api/permissions/typeId?q=user==user1,role=in=(role1,role2)&page=2&pageSize=10\",\"next\":\"http://localhost/api/permissions/typeId?q=user==user1,role=in=(role1,role2)&page=3&pageSize=10\"},\"data\":{\"id\":\"typeId\",\"label\":\"typeLabel\",\"objects\":[{\"id\":\"typeId\",\"label\":\"label\",\"permissions\":[{\"role\":\"role1\",\"permission\":\"READ\"},{\"role\":\"role2\",\"permission\":\"WRITE\"}]}]}}");
  }

  @Test
  public void testGetAllPermissionsForUser() throws Exception {
    Set<LabelledPermission> objectPermissionResponses =
        Sets.newHashSet(
            LabelledPermission.create(
                user1,
                LabelledObjectIdentity.create(
                    "typeId", "entityTypeId", "typeLabel", "identifier", "label"),
                PermissionSet.READ,
                null),
            LabelledPermission.create(
                user1,
                LabelledObjectIdentity.create(
                    "typeId", "entityTypeId", "typeLabel", "identifier", "label"),
                WRITE,
                null));
    when(permissionsService.getPermissions(Sets.newHashSet(user1), false))
        .thenReturn(objectPermissionResponses);
    when(userRoleTools.getSids(Collections.singletonList("user1"), Collections.emptyList()))
        .thenReturn(Collections.singletonList(new PrincipalSid("user1")));
    PermissionsQuery permissionsQuery = new PermissionsQuery();
    permissionsQuery.setUsers(Collections.singletonList("user1"));

    MvcResult result =
        mockMvc
            .perform(get(BASE_URI + "?q=user==user1&includeInheritance=false"))
            .andExpect(status().isOk())
            .andReturn();

    assertEquals(
        result.getResponse().getContentAsString(),
        "{\"data\":{\"permissions\":[{\"user\":\"user1\",\"object\":{\"id\":\"identifier\",\"label\":\"label\"},\"type\":{\"id\":\"typeId\",\"entityType\":\"entityTypeId\",\"label\":\"typeLabel\"},\"permission\":\"WRITE\"},{\"user\":\"user1\",\"object\":{\"id\":\"identifier\",\"label\":\"label\"},\"type\":{\"id\":\"typeId\",\"entityType\":\"entityTypeId\",\"label\":\"typeLabel\"},\"permission\":\"READ\"}]}}");
  }

  @Test
  public void testUpdatePermission() throws Exception {
    when(entityHelper.getObjectIdentity("typeId", "identifier")).thenReturn(objectIdentity);
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

    Permission permission1 =
        Permission.create(
            new ObjectIdentityImpl("typeId", "identifier"), new PrincipalSid("test2"), READ);
    Permission permission2 =
        Permission.create(
            new ObjectIdentityImpl("typeId", "identifier"), new PrincipalSid("test"), WRITE);

    verify(permissionsService).updatePermissions(Sets.newHashSet(permission1, permission2));
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
    ObjectIdentity objectIdentity1 = new ObjectIdentityImpl("typeId", "rij1");
    ObjectIdentity objectIdentity2 = new ObjectIdentityImpl("typeId", "rij2");
    doReturn(objectIdentity1).when(entityHelper).getObjectIdentity("typeId", "rij1");
    doReturn(objectIdentity2).when(entityHelper).getObjectIdentity("typeId", "rij2");

    mockMvc
        .perform(
            patch(BASE_URI + "/typeId").contentType(APPLICATION_JSON_UTF8).content(requestJson))
        .andExpect(status().isNoContent());

    Permission permission1 =
        Permission.create(objectIdentity1, new GrantedAuthoritySid("ROLE_IT_VIEWER"), WRITE);
    Permission permission2 =
        Permission.create(objectIdentity1, new GrantedAuthoritySid("ROLE_IT_MANAGER"), READ);
    Permission permission3 =
        Permission.create(objectIdentity2, new GrantedAuthoritySid("ROLE_IT_VIEWER"), WRITE);

    verify(permissionsService)
        .updatePermissions(Sets.newHashSet(permission3, permission1, permission2));
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
    ObjectIdentity objectIdentity1 = new ObjectIdentityImpl("typeId", "rij1");
    ObjectIdentity objectIdentity2 = new ObjectIdentityImpl("typeId", "rij2");
    doReturn(objectIdentity1).when(entityHelper).getObjectIdentity("typeId", "rij1");
    doReturn(objectIdentity2).when(entityHelper).getObjectIdentity("typeId", "rij2");

    Permission permission1 =
        Permission.create(objectIdentity1, new GrantedAuthoritySid("ROLE_IT_VIEWER"), WRITE);
    Permission permission2 =
        Permission.create(objectIdentity1, new GrantedAuthoritySid("ROLE_IT_MANAGER"), READ);
    Permission permission3 =
        Permission.create(objectIdentity2, new GrantedAuthoritySid("ROLE_IT_VIEWER"), WRITE);

    mockMvc
        .perform(post(BASE_URI + "/typeId").contentType(APPLICATION_JSON_UTF8).content(requestJson))
        .andExpect(status().isCreated());

    verify(permissionsService)
        .createPermissions(Sets.newHashSet(permission3, permission1, permission2));
  }

  @Test
  public void testCreatePermission() throws Exception {
    when(entityHelper.getObjectIdentity("typeId", "identifier")).thenReturn(objectIdentity);
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

    Permission permission1 =
        Permission.create(
            new ObjectIdentityImpl("typeId", "identifier"), new PrincipalSid("test2"), READ);
    Permission permission2 =
        Permission.create(
            new ObjectIdentityImpl("typeId", "identifier"), new PrincipalSid("test"), WRITE);

    verify(permissionsService).createPermissions(Sets.newHashSet(permission1, permission2));
  }

  @Test
  public void testDeletePermission() throws Exception {
    when(entityHelper.getObjectIdentity("typeId", "identifier")).thenReturn(objectIdentity);
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
