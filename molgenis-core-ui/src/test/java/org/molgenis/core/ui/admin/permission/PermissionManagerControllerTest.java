package org.molgenis.core.ui.admin.permission;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.meta.AttributeType.LONG;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.plugin.model.PluginMetadata.PLUGIN;
import static org.molgenis.data.security.auth.RoleMetadata.ROLE;
import static org.molgenis.data.security.auth.UserMetaData.USER;
import static org.molgenis.security.core.PermissionSet.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import java.util.*;
import java.util.stream.Stream;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.core.ui.admin.permission.PermissionManagerControllerTest.Config;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.security.EntityIdentity;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.RoleMetadata;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.security.core.PermissionRegistry;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.security.permission.Permissions;
import org.molgenis.web.PluginController;
import org.molgenis.web.converter.GsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes = {Config.class, GsonConfig.class})
public class PermissionManagerControllerTest extends AbstractTestNGSpringContextTests {
  @Autowired private Config config;

  private MockMvc mockMvc;

  private User user1, user2;
  private PrincipalSid userSid;
  private Role role1, role2;
  private GrantedAuthoritySid roleSid;

  private Plugin plugin1, plugin2;
  private EntityType entityType1, entityType2, entityType3;
  private Package package1, package2, package3;

  private PluginIdentity pluginIdentity1;
  private PluginIdentity pluginIdentity2;

  private EntityTypeIdentity entityIdentity1;
  private EntityTypeIdentity entityIdentity2;
  private EntityTypeIdentity entityIdentity3;

  private PackageIdentity packageIdentity1;
  private PackageIdentity packageIdentity2;
  private PackageIdentity packageIdentity3;

  @Mock private Permission permissionWritemeta;
  @Mock private Permission permissionWrite;
  @Mock private Permission permissionRead;
  @Mock private Permission permissionCount;

  private enum TestPermission implements org.molgenis.security.core.Permission {
    READ,
    UPDATE,
    DELETE;

    @Override
    public String getDefaultDescription() {
      return "Description of " + toString();
    }
  }

  @Configuration
  public static class Config extends WebMvcConfigurerAdapter {
    @Mock(answer = RETURNS_DEEP_STUBS)
    DataService dataService;

    @Mock MutableAclService mutableAclService;
    @Mock MutableAclClassService mutableAclClassService;
    @Mock SystemEntityTypeRegistry systemEntityTypeRegistry;
    @Mock PermissionRegistry permissionRegistry;

    public Config() {
      initMocks(this);
    }

    @Bean
    public DataService dataService() {
      return dataService;
    }

    @Bean
    public MutableAclService mutableAclService() {
      return mutableAclService;
    }

    @Bean
    public MutableAclClassService mutableAclClassService() {
      return mutableAclClassService;
    }

    @Bean
    public SystemEntityTypeRegistry systemEntityTypeRegistry() {
      return systemEntityTypeRegistry;
    }

    @Bean
    public PermissionRegistry permissionRegistry() {
      return permissionRegistry;
    }

    @Bean
    public PermissionManagerController permissionManagerController() {
      return new PermissionManagerController(
          dataService(),
          mutableAclService(),
          mutableAclClassService(),
          systemEntityTypeRegistry(),
          permissionRegistry());
    }

    void resetMocks() {
      reset(
          dataService,
          mutableAclService,
          mutableAclClassService,
          systemEntityTypeRegistry,
          permissionRegistry);
    }
  }

  @Autowired private PermissionManagerController permissionManagerController;

  @Autowired private GsonHttpMessageConverter gsonHttpMessageConverter;

  @Autowired private DataService dataService;

  @Autowired private MutableAclService mutableAclService;

  @Autowired private SystemEntityTypeRegistry systemEntityTypeRegistry;

  @Autowired private MutableAclClassService mutableAclClassService;

  @Autowired private PermissionRegistry permissionRegistry;

  @BeforeMethod
  public void setUp() {
    config.resetMocks();
    MockitoAnnotations.initMocks(this);
    mockMvc =
        MockMvcBuilders.standaloneSetup(permissionManagerController)
            .setMessageConverters(gsonHttpMessageConverter)
            .build();

    user1 = when(mock(User.class).getId()).thenReturn("1").getMock();
    when(user1.isSuperuser()).thenReturn(true);
    when(user1.getUsername()).thenReturn("Ipsum");
    userSid = new PrincipalSid("Ipsum");
    user2 = when(mock(User.class).getId()).thenReturn("2").getMock();

    role1 = when(mock(Role.class).getName()).thenReturn("ONE").getMock();
    roleSid = new GrantedAuthoritySid("ROLE_ONE");
    role2 = when(mock(Role.class).getName()).thenReturn("TWO").getMock();

    plugin1 = when(mock(Plugin.class).getId()).thenReturn("1").getMock();
    plugin2 = when(mock(Plugin.class).getId()).thenReturn("2").getMock();

    pluginIdentity1 = new PluginIdentity(plugin1);
    pluginIdentity2 = new PluginIdentity(plugin2);

    entityType1 = when(mock(EntityType.class).getId()).thenReturn("1").getMock();
    entityType2 = when(mock(EntityType.class).getId()).thenReturn("2").getMock();
    entityType3 = when(mock(EntityType.class).getId()).thenReturn("3").getMock();
    when(entityType1.getLabel()).thenReturn("label1");
    when(entityType2.getLabel()).thenReturn("label2");
    when(entityType3.getLabel()).thenReturn("label3");

    entityIdentity1 = new EntityTypeIdentity(entityType1);
    entityIdentity2 = new EntityTypeIdentity(entityType2);
    entityIdentity3 = new EntityTypeIdentity(entityType3);

    package1 = when(mock(Package.class).getId()).thenReturn("1").getMock();
    package2 = when(mock(Package.class).getId()).thenReturn("2").getMock();
    package3 = when(mock(Package.class).getId()).thenReturn("3").getMock();

    packageIdentity1 = new PackageIdentity(package1);
    packageIdentity2 = new PackageIdentity(package2);
    packageIdentity3 = new PackageIdentity(package3);

    when(dataService.findAll(USER, User.class)).thenReturn(Stream.of(user1, user2));
    when(dataService.findAll(ROLE, Role.class)).thenReturn(Stream.of(role1, role2));
    when(dataService.query(ROLE, Role.class).eq(RoleMetadata.NAME, "ONE").findOne())
        .thenReturn(role1);
    when(dataService.findOneById(USER, "1", User.class)).thenReturn(user1);

    when(dataService.findAll(PLUGIN, Plugin.class)).thenReturn(Stream.of(plugin1, plugin2));
    when(dataService.findAll(ENTITY_TYPE_META_DATA, EntityType.class))
        .thenReturn(Stream.of(entityType1, entityType2, entityType3));
    when(dataService.findAll(PACKAGE, Package.class))
        .thenReturn(Stream.of(package1, package2, package3));

    when(permissionWritemeta.getMask()).thenReturn(WRITEMETA_MASK);
    when(permissionWrite.getMask()).thenReturn(WRITE_MASK);
    when(permissionCount.getMask()).thenReturn(COUNT_MASK);
    when(permissionRead.getMask()).thenReturn(READ_MASK);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void PermissionManagerController() {
    new PermissionManagerController(null, null, null, null, null);
  }

  @Test
  public void init() throws Exception {
    this.mockMvc
        .perform(get(PluginController.PLUGIN_URI_PREFIX + "/permissionmanager"))
        .andExpect(status().isOk())
        .andExpect(view().name("view-permissionmanager"))
        .andExpect(model().attribute("users", Arrays.asList(user2)))
        .andExpect(model().attribute("roles", Arrays.asList(role1, role2)));
  }

  @Test
  public void testGetUsers() {
    assertEquals(permissionManagerController.getUsers(), Arrays.asList(user1, user2));
  }

  @Test
  public void testGetRoles() {
    assertEquals(permissionManagerController.getRoles(), Arrays.asList(role1, role2));
  }

  @Test
  public void testGetPlugins() {
    assertEquals(permissionManagerController.getPlugins(), Arrays.asList(plugin1, plugin2));
  }

  @Test
  public void testGetPackages() {
    assertEquals(
        permissionManagerController.getPackages(), Arrays.asList(package1, package2, package3));
  }

  @Test
  public void testGetUserPluginPermissions() {
    MutableAcl acl1 = mock(MutableAcl.class);
    MutableAcl acl2 = mock(MutableAcl.class);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);

    when(ace1.getSid()).thenReturn(userSid);

    when(acl1.getEntries()).thenReturn(Collections.singletonList(ace1));
    when(acl2.getEntries()).thenReturn(Collections.emptyList());

    Map<ObjectIdentity, Acl> acls = new HashMap<>();
    acls.put(pluginIdentity1, acl1);
    acls.put(pluginIdentity2, acl2);
    when(mutableAclService.readAclsById(
            Arrays.asList(pluginIdentity1, pluginIdentity2), singletonList(userSid)))
        .thenReturn(acls);

    when(ace1.getPermission()).thenReturn(permissionRead);

    Permissions expected =
        Permissions.create(
            ImmutableSet.of("1", "2"), ImmutableMultimap.of(plugin1.getId(), "read"));
    assertEquals(permissionManagerController.getUserPluginPermissions("Ipsum"), expected);
  }

  @Test
  public void testGetRolePluginPermissions() {
    MutableAcl acl1 = mock(MutableAcl.class);
    MutableAcl acl2 = mock(MutableAcl.class);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);

    when(ace1.getSid()).thenReturn(roleSid);

    when(acl1.getEntries()).thenReturn(Collections.singletonList(ace1));
    when(acl2.getEntries()).thenReturn(Collections.emptyList());

    Map<ObjectIdentity, Acl> acls = new HashMap<>();
    acls.put(pluginIdentity1, acl1);
    acls.put(pluginIdentity2, acl2);
    when(mutableAclService.readAclsById(
            Arrays.asList(pluginIdentity1, pluginIdentity2), singletonList(roleSid)))
        .thenReturn(acls);

    when(ace1.getPermission()).thenReturn(permissionRead);

    Permissions expected =
        Permissions.create(
            ImmutableSet.of("1", "2"), ImmutableMultimap.of(entityType1.getId(), "read"));
    Permissions actual = permissionManagerController.getRolePluginPermissions("ONE");
    assertEquals(actual, expected);
  }

  @Test
  public void testGetUserEntityClassPermissions() {
    MutableAcl acl1 = mock(MutableAcl.class);
    MutableAcl acl2 = mock(MutableAcl.class);
    MutableAcl acl3 = mock(MutableAcl.class);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    AccessControlEntry ace2 = mock(AccessControlEntry.class);

    when(ace1.getSid()).thenReturn(userSid);
    when(ace2.getSid()).thenReturn(userSid);

    when(acl1.getEntries()).thenReturn(Collections.singletonList(ace1));
    when(acl2.getEntries()).thenReturn(Collections.singletonList(ace2));
    when(acl3.getEntries()).thenReturn(Collections.emptyList());

    Map<ObjectIdentity, Acl> acls = new HashMap<>();
    acls.put(entityIdentity1, acl1);
    acls.put(entityIdentity2, acl2);
    acls.put(entityIdentity3, acl3);
    when(mutableAclService.readAclsById(
            Arrays.asList(entityIdentity1, entityIdentity2, entityIdentity3),
            singletonList(userSid)))
        .thenReturn(acls);

    when(ace1.getPermission()).thenReturn(permissionWritemeta);
    when(ace2.getPermission()).thenReturn(permissionCount);

    Permissions expected =
        Permissions.create(
            ImmutableSet.of("1", "2", "3"),
            ImmutableMultimap.of(entityType1.getId(), "writemeta", entityType2.getId(), "count"));

    assertEquals(permissionManagerController.getUserEntityClassPermissions("Ipsum"), expected);
  }

  @Test
  public void testGetRoleEntityTypePermissions() {
    MutableAcl acl1 = mock(MutableAcl.class);
    MutableAcl acl2 = mock(MutableAcl.class);
    MutableAcl acl3 = mock(MutableAcl.class);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    AccessControlEntry ace2 = mock(AccessControlEntry.class);

    when(ace1.getSid()).thenReturn(roleSid);
    when(ace2.getSid()).thenReturn(roleSid);

    when(acl1.getEntries()).thenReturn(Collections.singletonList(ace1));
    when(acl2.getEntries()).thenReturn(Collections.singletonList(ace2));
    when(acl3.getEntries()).thenReturn(Collections.emptyList());

    Map<ObjectIdentity, Acl> acls = new HashMap<>();
    acls.put(entityIdentity1, acl1);
    acls.put(entityIdentity2, acl2);
    acls.put(entityIdentity3, acl3);
    when(mutableAclService.readAclsById(
            Arrays.asList(entityIdentity1, entityIdentity2, entityIdentity3),
            singletonList(roleSid)))
        .thenReturn(acls);

    when(ace1.getPermission()).thenReturn(permissionWrite);
    when(ace2.getPermission()).thenReturn(permissionRead);

    Permissions expected =
        Permissions.create(
            ImmutableSet.of("1", "2", "3"),
            ImmutableMultimap.of(entityType1.getId(), "write", entityType2.getId(), "read"));

    assertEquals(permissionManagerController.getRoleEntityClassPermissions("ONE"), expected);
  }

  @Test
  public void testGetUserPackagePermissions() {
    MutableAcl acl1 = mock(MutableAcl.class);
    MutableAcl acl2 = mock(MutableAcl.class);
    MutableAcl acl3 = mock(MutableAcl.class);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    AccessControlEntry ace2 = mock(AccessControlEntry.class);

    when(ace1.getSid()).thenReturn(userSid);
    when(ace2.getSid()).thenReturn(userSid);

    when(acl1.getEntries()).thenReturn(Collections.singletonList(ace1));
    when(acl2.getEntries()).thenReturn(Collections.singletonList(ace2));
    when(acl3.getEntries()).thenReturn(Collections.emptyList());

    Map<ObjectIdentity, Acl> acls = new HashMap<>();
    acls.put(packageIdentity1, acl1);
    acls.put(packageIdentity2, acl2);
    acls.put(packageIdentity3, acl3);
    when(mutableAclService.readAclsById(
            Arrays.asList(packageIdentity1, packageIdentity2, packageIdentity3),
            singletonList(userSid)))
        .thenReturn(acls);

    when(ace1.getPermission()).thenReturn(permissionWritemeta);
    when(ace2.getPermission()).thenReturn(permissionCount);

    Permissions expected =
        Permissions.create(
            ImmutableSet.of("1", "2", "3"),
            ImmutableMultimap.of(package1.getId(), "writemeta", package2.getId(), "count"));

    assertEquals(permissionManagerController.getUserPackagePermissions("Ipsum"), expected);
  }

  @Test
  public void testGetRolePackagePermissions() {
    MutableAcl acl1 = mock(MutableAcl.class);
    MutableAcl acl2 = mock(MutableAcl.class);
    MutableAcl acl3 = mock(MutableAcl.class);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    AccessControlEntry ace2 = mock(AccessControlEntry.class);

    when(ace1.getSid()).thenReturn(roleSid);
    when(ace2.getSid()).thenReturn(roleSid);

    when(acl1.getEntries()).thenReturn(Collections.singletonList(ace1));
    when(acl2.getEntries()).thenReturn(Collections.singletonList(ace2));
    when(acl3.getEntries()).thenReturn(Collections.emptyList());

    Map<ObjectIdentity, Acl> acls = new HashMap<>();
    acls.put(packageIdentity1, acl1);
    acls.put(packageIdentity2, acl2);
    acls.put(packageIdentity3, acl3);
    when(mutableAclService.readAclsById(
            Arrays.asList(packageIdentity1, packageIdentity2, packageIdentity3),
            singletonList(roleSid)))
        .thenReturn(acls);

    when(ace1.getPermission()).thenReturn(permissionWrite);
    when(ace2.getPermission()).thenReturn(permissionRead);

    Permissions expected =
        Permissions.create(
            ImmutableSet.of("1", "2", "3"),
            ImmutableMultimap.of(package1.getId(), "write", package2.getId(), "read"));

    assertEquals(permissionManagerController.getRolePackagePermissions("ONE"), expected);
  }

  @Test
  public void testUpdateRolePluginPermissions() {
    WebRequest webRequest = mock(WebRequest.class);

    when(webRequest.getParameter("radio-1")).thenReturn("read");
    when(webRequest.getParameter("radio-2")).thenReturn("none");

    MutableAcl acl1 = mock(MutableAcl.class);
    MutableAcl acl2 = mock(MutableAcl.class);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);

    when(ace1.getSid()).thenReturn(roleSid);

    when(acl1.getEntries()).thenReturn(Collections.singletonList(ace1));
    when(acl2.getEntries()).thenReturn(Collections.emptyList());

    when(mutableAclService.readAclById(pluginIdentity1, singletonList(roleSid))).thenReturn(acl1);
    when(mutableAclService.readAclById(pluginIdentity2, singletonList(roleSid))).thenReturn(acl2);

    permissionManagerController.updateRolePluginPermissions("ONE", webRequest);

    verify(acl1).insertAce(0, PermissionSet.READ, roleSid, true);

    verify(mutableAclService).updateAcl(acl1);
  }

  @Test
  public void testUpdateUserPluginPermissions() {
    WebRequest webRequest = mock(WebRequest.class);

    when(webRequest.getParameter("radio-1")).thenReturn("read");
    when(webRequest.getParameter("radio-2")).thenReturn("none");

    ObjectIdentity objectIdentity1 = new PluginIdentity(plugin1);
    ObjectIdentity objectIdentity2 = new PluginIdentity(plugin2);

    MutableAcl acl1 = mock(MutableAcl.class);
    MutableAcl acl2 = mock(MutableAcl.class);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);

    when(ace1.getSid()).thenReturn(userSid);

    when(acl1.getEntries()).thenReturn(Collections.singletonList(ace1));
    when(acl2.getEntries()).thenReturn(Collections.emptyList());

    when(mutableAclService.readAclById(objectIdentity1, singletonList(userSid))).thenReturn(acl1);
    when(mutableAclService.readAclById(objectIdentity2, singletonList(userSid))).thenReturn(acl2);

    permissionManagerController.updateUserPluginPermissions("Ipsum", webRequest);

    verify(acl1).insertAce(0, PermissionSet.READ, userSid, true);

    verify(mutableAclService).updateAcl(acl1);
  }

  @Test
  public void testUpdateRoleEntityTypePermissions() {
    WebRequest webRequest = mock(WebRequest.class);

    when(webRequest.getParameter("radio-1")).thenReturn("write");
    when(webRequest.getParameter("radio-2")).thenReturn("none");
    when(webRequest.getParameter("radio-3")).thenReturn("read");

    MutableAcl acl1 = mock(MutableAcl.class);
    MutableAcl acl2 = mock(MutableAcl.class);
    MutableAcl acl3 = mock(MutableAcl.class);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    AccessControlEntry ace2 = mock(AccessControlEntry.class);

    GrantedAuthoritySid sid = new GrantedAuthoritySid("ROLE_ONE");

    when(ace1.getSid()).thenReturn(sid);
    when(ace2.getSid()).thenReturn(sid);

    when(acl1.getEntries()).thenReturn(Collections.singletonList(ace1));
    when(acl2.getEntries()).thenReturn(Collections.singletonList(ace2));
    when(acl3.getEntries()).thenReturn(Collections.emptyList());

    when(mutableAclService.readAclById(entityIdentity1, singletonList(sid))).thenReturn(acl1);
    when(mutableAclService.readAclById(entityIdentity2, singletonList(sid))).thenReturn(acl2);
    when(mutableAclService.readAclById(entityIdentity3, singletonList(sid))).thenReturn(acl3);

    permissionManagerController.updateRoleEntityClassPermissions("ONE", webRequest);

    verify(acl1).deleteAce(0);
    verify(acl1).insertAce(0, PermissionSet.WRITE, sid, true);
    verify(acl2).deleteAce(0);
    verify(acl3).insertAce(0, PermissionSet.READ, sid, true);

    verify(mutableAclService).updateAcl(acl1);
    verify(mutableAclService).updateAcl(acl2);
    verify(mutableAclService).updateAcl(acl3);
  }

  @Test
  public void testUpdateUserEntityClassPermissions() {
    WebRequest webRequest = mock(WebRequest.class);

    when(webRequest.getParameter("radio-1")).thenReturn("write");
    when(webRequest.getParameter("radio-2")).thenReturn("none");
    when(webRequest.getParameter("radio-3")).thenReturn("read");

    ObjectIdentity objectIdentity1 = new EntityTypeIdentity(entityType1);
    ObjectIdentity objectIdentity2 = new EntityTypeIdentity(entityType2);
    ObjectIdentity objectIdentity3 = new EntityTypeIdentity(entityType3);

    MutableAcl acl1 = mock(MutableAcl.class);
    MutableAcl acl2 = mock(MutableAcl.class);
    MutableAcl acl3 = mock(MutableAcl.class);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    AccessControlEntry ace2 = mock(AccessControlEntry.class);

    PrincipalSid sid = new PrincipalSid("Ipsum");

    when(ace1.getSid()).thenReturn(sid);
    when(ace2.getSid()).thenReturn(sid);

    when(acl1.getEntries()).thenReturn(Collections.singletonList(ace1));
    when(acl2.getEntries()).thenReturn(Collections.singletonList(ace2));
    when(acl3.getEntries()).thenReturn(Collections.emptyList());

    when(mutableAclService.readAclById(objectIdentity1, singletonList(sid))).thenReturn(acl1);
    when(mutableAclService.readAclById(objectIdentity2, singletonList(sid))).thenReturn(acl2);
    when(mutableAclService.readAclById(objectIdentity3, singletonList(sid))).thenReturn(acl3);

    permissionManagerController.updateUserEntityClassPermissions("Ipsum", webRequest);

    verify(acl1).deleteAce(0);
    verify(acl1).insertAce(0, PermissionSet.WRITE, sid, true);
    verify(acl2).deleteAce(0);
    verify(acl3).insertAce(0, PermissionSet.READ, sid, true);

    verify(mutableAclService).updateAcl(acl1);
    verify(mutableAclService).updateAcl(acl2);
    verify(mutableAclService).updateAcl(acl3);
  }

  @Test
  public void testUpdateRolePackagePermissions() {
    WebRequest webRequest = mock(WebRequest.class);

    when(webRequest.getParameter("radio-1")).thenReturn("write");
    when(webRequest.getParameter("radio-2")).thenReturn("none");
    when(webRequest.getParameter("radio-3")).thenReturn("read");

    MutableAcl acl1 = mock(MutableAcl.class);
    MutableAcl acl2 = mock(MutableAcl.class);
    MutableAcl acl3 = mock(MutableAcl.class);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    AccessControlEntry ace2 = mock(AccessControlEntry.class);

    GrantedAuthoritySid sid = new GrantedAuthoritySid("ROLE_ONE");

    when(ace1.getSid()).thenReturn(sid);
    when(ace2.getSid()).thenReturn(sid);

    when(acl1.getEntries()).thenReturn(Collections.singletonList(ace1));
    when(acl2.getEntries()).thenReturn(Collections.singletonList(ace2));
    when(acl3.getEntries()).thenReturn(Collections.emptyList());

    when(mutableAclService.readAclById(packageIdentity1, singletonList(sid))).thenReturn(acl1);
    when(mutableAclService.readAclById(packageIdentity2, singletonList(sid))).thenReturn(acl2);
    when(mutableAclService.readAclById(packageIdentity3, singletonList(sid))).thenReturn(acl3);

    permissionManagerController.updateRolePackagePermissions("ONE", webRequest);

    verify(acl1).deleteAce(0);
    verify(acl1).insertAce(0, PermissionSet.WRITE, sid, true);
    verify(acl2).deleteAce(0);
    verify(acl3).insertAce(0, PermissionSet.READ, sid, true);

    verify(mutableAclService).updateAcl(acl1);
    verify(mutableAclService).updateAcl(acl2);
    verify(mutableAclService).updateAcl(acl3);
  }

  @Test
  public void testUpdateUserPackagePermissions() {
    WebRequest webRequest = mock(WebRequest.class);

    when(webRequest.getParameter("radio-1")).thenReturn("write");
    when(webRequest.getParameter("radio-2")).thenReturn("none");
    when(webRequest.getParameter("radio-3")).thenReturn("read");

    MutableAcl acl1 = mock(MutableAcl.class);
    MutableAcl acl2 = mock(MutableAcl.class);
    MutableAcl acl3 = mock(MutableAcl.class);

    AccessControlEntry ace1 = mock(AccessControlEntry.class);
    AccessControlEntry ace2 = mock(AccessControlEntry.class);

    PrincipalSid sid = new PrincipalSid("Ipsum");

    when(ace1.getSid()).thenReturn(sid);
    when(ace2.getSid()).thenReturn(sid);

    when(acl1.getEntries()).thenReturn(Collections.singletonList(ace1));
    when(acl2.getEntries()).thenReturn(Collections.singletonList(ace2));
    when(acl3.getEntries()).thenReturn(Collections.emptyList());

    when(mutableAclService.readAclById(packageIdentity1, singletonList(sid))).thenReturn(acl1);
    when(mutableAclService.readAclById(packageIdentity2, singletonList(sid))).thenReturn(acl2);
    when(mutableAclService.readAclById(packageIdentity3, singletonList(sid))).thenReturn(acl3);

    permissionManagerController.updateUserPackagePermissions("Ipsum", webRequest);

    verify(acl1).deleteAce(0);
    verify(acl1).insertAce(0, PermissionSet.WRITE, sid, true);
    verify(acl2).deleteAce(0);
    verify(acl3).insertAce(0, PermissionSet.READ, sid, true);

    verify(mutableAclService).updateAcl(acl1);
    verify(mutableAclService).updateAcl(acl2);
    verify(mutableAclService).updateAcl(acl3);
  }

  @Test
  public void testUpdateEntityClassRlsEnableRls() {
    String entityTypeId = "entityTypeId";
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    Attribute idAttribute = when(mock(Attribute.class).getDataType()).thenReturn(LONG).getMock();
    when(entityType.getIdAttribute()).thenReturn(idAttribute);
    when(dataService.getEntityType(entityTypeId)).thenReturn(entityType);
    Entity entity = when(mock(Entity.class).getEntityType()).thenReturn(entityType).getMock();
    when(entity.getIdValue()).thenReturn("entityId");
    when(dataService.findAll(entityTypeId)).thenReturn(Stream.of(entity));

    EntityTypeRlsRequest entityTypeRlsRequest = new EntityTypeRlsRequest(entityTypeId, true);
    permissionManagerController.updateEntityClassRls(entityTypeRlsRequest);
    verify(mutableAclClassService).createAclClass("entity-entityTypeId", Long.class);
    verify(mutableAclService).createAcl(new EntityIdentity(entity));
  }

  @Test
  public void testUpdateEntityClassRlsDisableRls() {
    String entityTypeId = "entityTypeId";
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    Attribute idAttribute = when(mock(Attribute.class).getDataType()).thenReturn(LONG).getMock();
    when(entityType.getIdAttribute()).thenReturn(idAttribute);
    when(dataService.getEntityType(entityTypeId)).thenReturn(entityType);
    Entity entity = when(mock(Entity.class).getEntityType()).thenReturn(entityType).getMock();
    when(entity.getIdValue()).thenReturn("entityId");
    when(dataService.findAll(entityTypeId)).thenReturn(Stream.of(entity));
    when(mutableAclClassService.hasAclClass("entity-entityTypeId")).thenReturn(true);

    EntityTypeRlsRequest entityTypeRlsRequest = new EntityTypeRlsRequest(entityTypeId, false);
    permissionManagerController.updateEntityClassRls(entityTypeRlsRequest);
    verify(mutableAclClassService).deleteAclClass("entity-entityTypeId");
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Updating system entity type not allowed")
  public void testUpdateEntityClassRlsSystemEntityType() {
    when(systemEntityTypeRegistry.hasSystemEntityType("entityTypeId")).thenReturn(true);
    EntityTypeRlsRequest entityTypeRlsRequest = new EntityTypeRlsRequest("entityTypeId", true);
    permissionManagerController.updateEntityClassRls(entityTypeRlsRequest);
  }

  @Test
  public void testGetPermissionSets() {
    when(permissionRegistry.getPermissionSets())
        .thenReturn(
            ImmutableMap.of(
                PermissionSet.READ,
                singleton(TestPermission.READ),
                PermissionSet.WRITE,
                ImmutableSet.of(
                    TestPermission.READ, TestPermission.UPDATE, TestPermission.DELETE)));

    List<PermissionSetResponse> actual = permissionManagerController.getPermissionSets();
    List<PermissionSetResponse> expected =
        ImmutableList.of(
            PermissionSetResponse.create(
                "Read",
                ImmutableList.of(
                    PermissionResponse.create("TestPermission", "READ", "Description of READ"))),
            PermissionSetResponse.create(
                "Write",
                ImmutableList.of(
                    PermissionResponse.create("TestPermission", "READ", "Description of READ"),
                    PermissionResponse.create("TestPermission", "UPDATE", "Description of UPDATE"),
                    PermissionResponse.create(
                        "TestPermission", "DELETE", "Description of DELETE"))));
    assertEquals(actual, expected);
  }
}
