package org.molgenis.core.ui.admin.permission;

import org.mockito.Mock;
import org.molgenis.core.ui.admin.permission.PermissionManagerControllerTest.Config;
import org.molgenis.core.ui.util.GsonConfig;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginPermission;
import org.molgenis.data.security.EntityIdentity;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.auth.Group;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.security.permission.Permissions;
import org.molgenis.web.PluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.security.acls.domain.CumulativePermission;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.meta.AttributeType.LONG;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.plugin.model.PluginMetadata.PLUGIN;
import static org.molgenis.data.security.auth.GroupMetaData.GROUP;
import static org.molgenis.data.security.auth.UserMetaData.USER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.testng.Assert.assertEquals;

@WebAppConfiguration
@ContextConfiguration(classes = { Config.class, GsonConfig.class })
public class PermissionManagerControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private Config config;

	private MockMvc mockMvc;

	private User user1, user2;
	private PrincipalSid userSid;
	private Group group1, group2;
	private GrantedAuthoritySid groupSid;

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

	private PluginPermission pluginPermissionRead;
	private CumulativePermission cumulativeEntityPermissionWritemeta;
	private CumulativePermission cumulativeEntityPermissionWrite;
	private CumulativePermission cumulativeEntityPermissionRead;
	private CumulativePermission cumulativeEntityPermissionCount;

	@Configuration
	public static class Config extends WebMvcConfigurerAdapter
	{
		@Mock
		DataService dataService;
		@Mock
		MutableAclService mutableAclService;
		@Mock
		MutableAclClassService mutableAclClassService;
		@Mock
		SystemEntityTypeRegistry systemEntityTypeRegistry;

		public Config()
		{
			initMocks(this);
		}

		@Bean
		public DataService dataService()
		{
			return dataService;
		}

		@Bean
		public MutableAclService mutableAclService()
		{
			return mutableAclService;
		}

		@Bean
		public MutableAclClassService mutableAclClassService()
		{
			return mutableAclClassService;
		}

		@Bean
		public SystemEntityTypeRegistry systemEntityTypeRegistry()
		{
			return systemEntityTypeRegistry;
		}

		@Bean
		public PermissionManagerController permissionManagerController()
		{
			return new PermissionManagerController(dataService(), mutableAclService(), mutableAclClassService(),
					systemEntityTypeRegistry());
		}

		void resetMocks()
		{
			reset(dataService, mutableAclService, mutableAclClassService, systemEntityTypeRegistry);
		}
	}

	@Autowired
	private PermissionManagerController permissionManagerController;

	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	@Autowired
	private DataService dataService;

	@Autowired
	private MutableAclService mutableAclService;

	@Autowired
	private SystemEntityTypeRegistry systemEntityTypeRegistry;

	@Autowired
	private MutableAclClassService mutableAclClassService;

	@BeforeMethod
	public void setUp()
	{
		config.resetMocks();
		mockMvc = MockMvcBuilders.standaloneSetup(permissionManagerController)
								 .setMessageConverters(gsonHttpMessageConverter)
								 .build();

		user1 = when(mock(User.class).getId()).thenReturn("1").getMock();
		when(user1.isSuperuser()).thenReturn(true);
		when(user1.getUsername()).thenReturn("Ipsum");
		userSid = new PrincipalSid("Ipsum");
		user2 = when(mock(User.class).getId()).thenReturn("2").getMock();

		group1 = when(mock(Group.class).getId()).thenReturn("1").getMock();
		groupSid = new GrantedAuthoritySid("ROLE_1");
		group2 = when(mock(Group.class).getId()).thenReturn("2").getMock();

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
		when(dataService.findAll(GROUP, Group.class)).thenReturn(Stream.of(group1, group2));
		when(dataService.findOneById(GROUP, "1", Group.class)).thenReturn(group1);
		when(dataService.findOneById(USER, "1", User.class)).thenReturn(user1);

		when(dataService.findAll(PLUGIN, Plugin.class)).thenReturn(Stream.of(plugin1, plugin2));
		when(dataService.findAll(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(
				Stream.of(entityType1, entityType2, entityType3));
		when(dataService.findAll(PACKAGE, Package.class)).thenReturn(Stream.of(package1, package2, package3));

		pluginPermissionRead = PluginPermission.READ;

		cumulativeEntityPermissionWritemeta = new CumulativePermission();
		cumulativeEntityPermissionWritemeta.set(EntityTypePermission.WRITEMETA)
										   .set(EntityTypePermission.WRITE)
										   .set(EntityTypePermission.READ)
										   .set(EntityTypePermission.COUNT);
		cumulativeEntityPermissionWrite = new CumulativePermission();
		cumulativeEntityPermissionWrite.set(EntityTypePermission.WRITE)
									   .set(EntityTypePermission.READ)
									   .set(EntityTypePermission.COUNT);
		cumulativeEntityPermissionRead = new CumulativePermission();
		cumulativeEntityPermissionRead.set(EntityTypePermission.READ).set(EntityTypePermission.COUNT);
		cumulativeEntityPermissionCount = new CumulativePermission();
		cumulativeEntityPermissionCount.set(EntityTypePermission.COUNT);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void PermissionManagerController()
	{
		new PermissionManagerController(null, null, null, null);
	}

	@Test
	public void init() throws Exception
	{
		this.mockMvc.perform(get(PluginController.PLUGIN_URI_PREFIX + "/permissionmanager"))
					.andExpect(status().isOk())
					.andExpect(view().name("view-permissionmanager"))
					.andExpect(model().attribute("users", Arrays.asList(user2)))
					.andExpect(model().attribute("groups", Arrays.asList(group1, group2)));
	}

	@Test
	public void testGetUsers()
	{
		assertEquals(permissionManagerController.getUsers(), Arrays.asList(user1, user2));
	}

	@Test
	public void testGetGroups()
	{
		assertEquals(permissionManagerController.getGroups(), Arrays.asList(group1, group2));
	}

	@Test
	public void testGetPlugins()
	{
		assertEquals(permissionManagerController.getPlugins(), Arrays.asList(plugin1, plugin2));
	}

	@Test
	public void testGetPackages()
	{
		assertEquals(permissionManagerController.getPackages(), Arrays.asList(package1, package2, package3));
	}

	@Test
	public void testGetUserPluginPermissions()
	{
		MutableAcl acl1 = mock(MutableAcl.class);
		MutableAcl acl2 = mock(MutableAcl.class);

		AccessControlEntry ace1 = mock(AccessControlEntry.class);

		when(ace1.getSid()).thenReturn(userSid);

		when(acl1.getEntries()).thenReturn(Collections.singletonList(ace1));
		when(acl2.getEntries()).thenReturn(Collections.emptyList());

		Map<ObjectIdentity, Acl> acls = new HashMap<>();
		acls.put(pluginIdentity1, acl1);
		acls.put(pluginIdentity2, acl2);
		when(mutableAclService.readAclsById(Arrays.asList(pluginIdentity1, pluginIdentity2),
				singletonList(userSid))).thenReturn(acls);

		when(ace1.getPermission()).thenReturn(pluginPermissionRead);

		Permissions expected = new Permissions();
		org.molgenis.security.permission.Permission permission = new org.molgenis.security.permission.Permission();
		permission.setType("read");
		expected.setUserId("Ipsum");
		expected.addUserPermission(plugin1.getId(), permission);
		Map<String, String> ids = new HashMap<>();
		ids.put("1", "1");
		ids.put("2", "2");
		expected.setEntityIds(ids);

		assertEquals(permissionManagerController.getUserPluginPermissions("1"), expected);
	}

	@Test
	public void testGetGroupPluginPermissions()
	{
		MutableAcl acl1 = mock(MutableAcl.class);
		MutableAcl acl2 = mock(MutableAcl.class);

		AccessControlEntry ace1 = mock(AccessControlEntry.class);

		when(ace1.getSid()).thenReturn(groupSid);

		when(acl1.getEntries()).thenReturn(Collections.singletonList(ace1));
		when(acl2.getEntries()).thenReturn(Collections.emptyList());

		Map<ObjectIdentity, Acl> acls = new HashMap<>();
		acls.put(pluginIdentity1, acl1);
		acls.put(pluginIdentity2, acl2);
		when(mutableAclService.readAclsById(Arrays.asList(pluginIdentity1, pluginIdentity2),
				singletonList(groupSid))).thenReturn(acls);

		when(ace1.getPermission()).thenReturn(pluginPermissionRead);

		Permissions expected = new Permissions();
		org.molgenis.security.permission.Permission permission = new org.molgenis.security.permission.Permission();
		permission.setType("read");
		expected.setGroupId("1");
		expected.addGroupPermission(entityType1.getId(), permission);
		Map<String, String> ids = new HashMap<>();
		ids.put("1", "1");
		ids.put("2", "2");
		expected.setEntityIds(ids);

		assertEquals(permissionManagerController.getGroupPluginPermissions("1"), expected);
	}

	@Test
	public void testGetUserEntityClassPermissions()
	{
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
		when(mutableAclService.readAclsById(Arrays.asList(entityIdentity1, entityIdentity2, entityIdentity3),
				singletonList(userSid))).thenReturn(acls);

		when(ace1.getPermission()).thenReturn(cumulativeEntityPermissionWritemeta);
		when(ace2.getPermission()).thenReturn(cumulativeEntityPermissionCount);

		Permissions expected = new Permissions();
		org.molgenis.security.permission.Permission permission1 = new org.molgenis.security.permission.Permission();
		permission1.setType("writemeta");
		org.molgenis.security.permission.Permission permission2 = new org.molgenis.security.permission.Permission();
		permission2.setType("count");
		expected.setUserId("Ipsum");
		expected.addUserPermission(entityType1.getId(), permission1);
		expected.addUserPermission(entityType2.getId(), permission2);
		Map<String, String> ids = new HashMap<>();
		ids.put("1", "1");
		ids.put("2", "2");
		ids.put("3", "3");
		expected.setEntityIds(ids);

		assertEquals(permissionManagerController.getUserEntityClassPermissions("1"), expected);
	}

	@Test
	public void testGetGroupEntityTypePermissions()
	{
		MutableAcl acl1 = mock(MutableAcl.class);
		MutableAcl acl2 = mock(MutableAcl.class);
		MutableAcl acl3 = mock(MutableAcl.class);

		AccessControlEntry ace1 = mock(AccessControlEntry.class);
		AccessControlEntry ace2 = mock(AccessControlEntry.class);

		when(ace1.getSid()).thenReturn(groupSid);
		when(ace2.getSid()).thenReturn(groupSid);

		when(acl1.getEntries()).thenReturn(Collections.singletonList(ace1));
		when(acl2.getEntries()).thenReturn(Collections.singletonList(ace2));
		when(acl3.getEntries()).thenReturn(Collections.emptyList());

		Map<ObjectIdentity, Acl> acls = new HashMap<>();
		acls.put(entityIdentity1, acl1);
		acls.put(entityIdentity2, acl2);
		acls.put(entityIdentity3, acl3);
		when(mutableAclService.readAclsById(Arrays.asList(entityIdentity1, entityIdentity2, entityIdentity3),
				singletonList(groupSid))).thenReturn(acls);

		when(ace1.getPermission()).thenReturn(cumulativeEntityPermissionWrite);
		when(ace2.getPermission()).thenReturn(cumulativeEntityPermissionRead);

		Permissions expected = new Permissions();
		org.molgenis.security.permission.Permission permission1 = new org.molgenis.security.permission.Permission();
		permission1.setType("write");
		org.molgenis.security.permission.Permission permission2 = new org.molgenis.security.permission.Permission();
		permission2.setType("read");
		expected.setGroupId("1");
		expected.addGroupPermission(entityType1.getId(), permission1);
		expected.addGroupPermission(entityType2.getId(), permission2);
		Map<String, String> ids = new HashMap<>();
		ids.put("1", "1");
		ids.put("2", "2");
		ids.put("3", "3");
		expected.setEntityIds(ids);

		assertEquals(permissionManagerController.getGroupEntityClassPermissions("1"), expected);
	}

	@Test
	public void testGetUserPackagePermissions()
	{
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
		when(mutableAclService.readAclsById(Arrays.asList(packageIdentity1, packageIdentity2, packageIdentity3),
				singletonList(userSid))).thenReturn(acls);

		when(ace1.getPermission()).thenReturn(cumulativeEntityPermissionWritemeta);
		when(ace2.getPermission()).thenReturn(cumulativeEntityPermissionCount);

		Permissions expected = new Permissions();
		org.molgenis.security.permission.Permission permission1 = new org.molgenis.security.permission.Permission();
		permission1.setType("writemeta");
		org.molgenis.security.permission.Permission permission2 = new org.molgenis.security.permission.Permission();
		permission2.setType("count");
		expected.setUserId("Ipsum");
		expected.addUserPermission(package1.getId(), permission1);
		expected.addUserPermission(package2.getId(), permission2);
		Map<String, String> ids = new HashMap<>();
		ids.put("1", "1");
		ids.put("2", "2");
		ids.put("3", "3");
		expected.setEntityIds(ids);

		assertEquals(permissionManagerController.getUserPackagePermissions("1"), expected);
	}

	@Test
	public void testGetGroupPackagePermissions()
	{
		MutableAcl acl1 = mock(MutableAcl.class);
		MutableAcl acl2 = mock(MutableAcl.class);
		MutableAcl acl3 = mock(MutableAcl.class);

		AccessControlEntry ace1 = mock(AccessControlEntry.class);
		AccessControlEntry ace2 = mock(AccessControlEntry.class);

		when(ace1.getSid()).thenReturn(groupSid);
		when(ace2.getSid()).thenReturn(groupSid);

		when(acl1.getEntries()).thenReturn(Collections.singletonList(ace1));
		when(acl2.getEntries()).thenReturn(Collections.singletonList(ace2));
		when(acl3.getEntries()).thenReturn(Collections.emptyList());

		Map<ObjectIdentity, Acl> acls = new HashMap<>();
		acls.put(packageIdentity1, acl1);
		acls.put(packageIdentity2, acl2);
		acls.put(packageIdentity3, acl3);
		when(mutableAclService.readAclsById(Arrays.asList(packageIdentity1, packageIdentity2, packageIdentity3),
				singletonList(groupSid))).thenReturn(acls);

		when(ace1.getPermission()).thenReturn(cumulativeEntityPermissionWrite);
		when(ace2.getPermission()).thenReturn(cumulativeEntityPermissionRead);

		Permissions expected = new Permissions();
		org.molgenis.security.permission.Permission permission1 = new org.molgenis.security.permission.Permission();
		permission1.setType("write");
		org.molgenis.security.permission.Permission permission2 = new org.molgenis.security.permission.Permission();
		permission2.setType("read");
		expected.setGroupId("1");
		expected.addGroupPermission(package1.getId(), permission1);
		expected.addGroupPermission(package2.getId(), permission2);
		Map<String, String> ids = new HashMap<>();
		ids.put("1", "1");
		ids.put("2", "2");
		ids.put("3", "3");
		expected.setEntityIds(ids);

		assertEquals(permissionManagerController.getGroupPackagePermissions("1"), expected);
	}

	@Test
	public void testUpdateGroupPluginPermissions()
	{
		WebRequest webRequest = mock(WebRequest.class);

		when(webRequest.getParameter("radio-1")).thenReturn("read");
		when(webRequest.getParameter("radio-2")).thenReturn("none");

		MutableAcl acl1 = mock(MutableAcl.class);
		MutableAcl acl2 = mock(MutableAcl.class);

		AccessControlEntry ace1 = mock(AccessControlEntry.class);

		when(ace1.getSid()).thenReturn(groupSid);

		when(acl1.getEntries()).thenReturn(Collections.singletonList(ace1));
		when(acl2.getEntries()).thenReturn(Collections.emptyList());

		when(mutableAclService.readAclById(pluginIdentity1, singletonList(groupSid))).thenReturn(acl1);
		when(mutableAclService.readAclById(pluginIdentity2, singletonList(groupSid))).thenReturn(acl2);

		permissionManagerController.updateGroupPluginPermissions("1", webRequest);

		verify(acl1).insertAce(0, pluginPermissionRead, groupSid, true);

		verify(mutableAclService).updateAcl(acl1);
	}

	@Test
	public void testUpdateUserPluginPermissions()
	{
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

		permissionManagerController.updateUserPluginPermissions("1", webRequest);

		verify(acl1).insertAce(0, pluginPermissionRead, userSid, true);

		verify(mutableAclService).updateAcl(acl1);
	}

	@Test
	public void testUpdateGroupEntityClassPermissions()
	{
		WebRequest webRequest = mock(WebRequest.class);

		when(webRequest.getParameter("radio-1")).thenReturn("write");
		when(webRequest.getParameter("radio-2")).thenReturn("none");
		when(webRequest.getParameter("radio-3")).thenReturn("read");

		MutableAcl acl1 = mock(MutableAcl.class);
		MutableAcl acl2 = mock(MutableAcl.class);
		MutableAcl acl3 = mock(MutableAcl.class);

		AccessControlEntry ace1 = mock(AccessControlEntry.class);
		AccessControlEntry ace2 = mock(AccessControlEntry.class);

		GrantedAuthoritySid sid = new GrantedAuthoritySid("ROLE_1");

		when(ace1.getSid()).thenReturn(sid);
		when(ace2.getSid()).thenReturn(sid);

		when(acl1.getEntries()).thenReturn(Collections.singletonList(ace1));
		when(acl2.getEntries()).thenReturn(Collections.singletonList(ace2));
		when(acl3.getEntries()).thenReturn(Collections.emptyList());

		when(mutableAclService.readAclById(entityIdentity1, singletonList(sid))).thenReturn(acl1);
		when(mutableAclService.readAclById(entityIdentity2, singletonList(sid))).thenReturn(acl2);
		when(mutableAclService.readAclById(entityIdentity3, singletonList(sid))).thenReturn(acl3);

		permissionManagerController.updateGroupEntityClassPermissions("1", webRequest);

		verify(acl1).deleteAce(0);
		verify(acl1).insertAce(0, cumulativeEntityPermissionWrite, sid, true);
		verify(acl2).deleteAce(0);
		verify(acl3).insertAce(0, cumulativeEntityPermissionRead, sid, true);

		verify(mutableAclService).updateAcl(acl1);
		verify(mutableAclService).updateAcl(acl2);
		verify(mutableAclService).updateAcl(acl3);
	}

	@Test
	public void testUpdateUserEntityClassPermissions()
	{
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

		permissionManagerController.updateUserEntityClassPermissions("1", webRequest);

		verify(acl1).deleteAce(0);
		verify(acl1).insertAce(0, cumulativeEntityPermissionWrite, sid, true);
		verify(acl2).deleteAce(0);
		verify(acl3).insertAce(0, cumulativeEntityPermissionRead, sid, true);

		verify(mutableAclService).updateAcl(acl1);
		verify(mutableAclService).updateAcl(acl2);
		verify(mutableAclService).updateAcl(acl3);
	}

	@Test
	public void testUpdateGroupPackagePermissions()
	{
		WebRequest webRequest = mock(WebRequest.class);

		when(webRequest.getParameter("radio-1")).thenReturn("write");
		when(webRequest.getParameter("radio-2")).thenReturn("none");
		when(webRequest.getParameter("radio-3")).thenReturn("read");

		MutableAcl acl1 = mock(MutableAcl.class);
		MutableAcl acl2 = mock(MutableAcl.class);
		MutableAcl acl3 = mock(MutableAcl.class);

		AccessControlEntry ace1 = mock(AccessControlEntry.class);
		AccessControlEntry ace2 = mock(AccessControlEntry.class);

		GrantedAuthoritySid sid = new GrantedAuthoritySid("ROLE_1");

		when(ace1.getSid()).thenReturn(sid);
		when(ace2.getSid()).thenReturn(sid);

		when(acl1.getEntries()).thenReturn(Collections.singletonList(ace1));
		when(acl2.getEntries()).thenReturn(Collections.singletonList(ace2));
		when(acl3.getEntries()).thenReturn(Collections.emptyList());

		when(mutableAclService.readAclById(packageIdentity1, singletonList(sid))).thenReturn(acl1);
		when(mutableAclService.readAclById(packageIdentity2, singletonList(sid))).thenReturn(acl2);
		when(mutableAclService.readAclById(packageIdentity3, singletonList(sid))).thenReturn(acl3);

		permissionManagerController.updateGroupPackagePermissions("1", webRequest);

		verify(acl1).deleteAce(0);
		verify(acl1).insertAce(0, cumulativeEntityPermissionWrite, sid, true);
		verify(acl2).deleteAce(0);
		verify(acl3).insertAce(0, cumulativeEntityPermissionRead, sid, true);

		verify(mutableAclService).updateAcl(acl1);
		verify(mutableAclService).updateAcl(acl2);
		verify(mutableAclService).updateAcl(acl3);
	}

	@Test
	public void testUpdateUserPackagePermissions()
	{
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

		permissionManagerController.updateUserPackagePermissions("1", webRequest);

		verify(acl1).deleteAce(0);
		verify(acl1).insertAce(0, cumulativeEntityPermissionWrite, sid, true);
		verify(acl2).deleteAce(0);
		verify(acl3).insertAce(0, cumulativeEntityPermissionRead, sid, true);

		verify(mutableAclService).updateAcl(acl1);
		verify(mutableAclService).updateAcl(acl2);
		verify(mutableAclService).updateAcl(acl3);
	}

	@Test
	public void testUpdateEntityClassRlsEnableRls()
	{
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
	public void testUpdateEntityClassRlsDisableRls()
	{
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

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Updating system entity type not allowed")
	public void testUpdateEntityClassRlsSystemEntityType()
	{
		when(systemEntityTypeRegistry.hasSystemEntityType("entityTypeId")).thenReturn(true);
		EntityTypeRlsRequest entityTypeRlsRequest = new EntityTypeRlsRequest("entityTypeId", true);
		permissionManagerController.updateEntityClassRls(entityTypeRlsRequest);
	}
}
