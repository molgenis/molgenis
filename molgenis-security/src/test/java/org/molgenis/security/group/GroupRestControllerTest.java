package org.molgenis.security.group;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.molgenis.data.security.auth.GroupPermission.*;
import static org.molgenis.security.group.AddGroupMemberCommand.addGroupMember;
import static org.molgenis.security.group.GroupCommand.createGroup;
import static org.molgenis.security.group.GroupRestController.GROUP_END_POINT;
import static org.molgenis.security.group.GroupRestController.TEMP_USER_END_POINT;
import static org.molgenis.security.group.UpdateGroupMemberCommand.updateGroupMember;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.mockito.Mock;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.security.GroupIdentity;
import org.molgenis.data.security.auth.*;
import org.molgenis.data.security.exception.NotAValidGroupRoleException;
import org.molgenis.data.security.permission.RoleMembershipService;
import org.molgenis.data.security.user.UserService;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.i18n.format.MessageFormatFactory;
import org.molgenis.i18n.test.exception.TestAllPropertiesMessageSource;
import org.molgenis.security.core.GroupValueFactory;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.core.model.GroupValue;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.molgenis.web.converter.GsonConfig;
import org.molgenis.web.exception.FallbackExceptionHandler;
import org.molgenis.web.exception.GlobalControllerExceptionHandler;
import org.molgenis.web.exception.SpringExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.LocaleResolver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {GroupRestControllerTest.Config.class, GsonConfig.class})
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class GroupRestControllerTest extends AbstractMockitoTestNGSpringContextTests {
  private final GroupValueFactory groupValueFactory = new GroupValueFactory();
  @Mock private GroupService groupService;
  @Mock private GroupPermissionService groupPermissionService;
  @Mock private RoleMembershipService roleMembershipService;
  @Mock private RoleService roleService;
  @Mock private UserService userService;

  @Mock private UserPermissionEvaluator userPermissionEvaluator;

  @Mock private RoleMembershipMetadata roleMembershipMetadata;
  @Mock private RoleMetadata roleMetadata;
  @Mock private GroupMetadata groupMetadata;
  @Mock private UserMetaData userMetadata;
  @Mock private Attribute attribute;

  @Mock private User user;
  @Mock private Group group;
  @Mock private Role viewer;
  @Mock private Role editor;
  @Mock private Role manager;
  @Mock private LocaleResolver localeResolver;
  @Mock private RoleMembership memberShip;

  private MockMvc mockMvc;

  @Autowired private GsonHttpMessageConverter gsonHttpMessageConverter;

  @Autowired private FallbackExceptionHandler fallbackExceptionHandler;

  @Autowired private SpringExceptionHandler springExceptionHandler;

  @Autowired private GlobalControllerExceptionHandler globalControllerExceptionHandler;

  @Autowired private Gson gson;

  @BeforeClass
  public void beforeClass() {
    TestAllPropertiesMessageSource messageSource =
        new TestAllPropertiesMessageSource(new MessageFormatFactory());
    messageSource.addMolgenisNamespaces("data-security", "data", "security");
    MessageSourceHolder.setMessageSource(messageSource);
  }

  @BeforeMethod
  public void beforeMethod() {
    GroupRestController groupRestController =
        new GroupRestController(
            groupValueFactory,
            groupService,
            roleMembershipService,
            roleService,
            userService,
            userPermissionEvaluator,
            groupPermissionService);
    mockMvc =
        MockMvcBuilders.standaloneSetup(groupRestController)
            .setMessageConverters(new FormHttpMessageConverter(), gsonHttpMessageConverter)
            .setLocaleResolver(localeResolver)
            .setControllerAdvice(
                globalControllerExceptionHandler, fallbackExceptionHandler, springExceptionHandler)
            .build();
  }

  @Test
  @WithMockUser("henkie")
  public void testCreateGroup() throws Exception {
    when(groupService.isGroupNameAvailable(any())).thenReturn(true);
    mockMvc
        .perform(
            post(GROUP_END_POINT)
                .contentType(APPLICATION_JSON_UTF8)
                .content(gson.toJson(createGroup("devs", "Developers"))))
        .andExpect(status().isCreated());

    GroupValue groupValue =
        groupValueFactory.createGroup(
            "devs", "Developers", null, true, ImmutableSet.of("Manager", "Editor", "Viewer"));
    verify(groupService).persist(groupValue);
    verify(groupPermissionService).grantDefaultPermissions(groupValue);
    verify(roleMembershipService).addUserToRole("henkie", "DEVS_MANAGER");
  }

  @Test
  @WithMockUser("henkie")
  public void testCreateGroupUnavailableGroupName() throws Exception {
    when(groupService.isGroupNameAvailable(any())).thenReturn(false);
    mockMvc
        .perform(
            post(GROUP_END_POINT)
                .contentType(APPLICATION_JSON_UTF8)
                .content(gson.toJson(createGroup("devs", "Developers"))))
        .andExpect(status().isBadRequest());

    verifyNoMoreInteractions(groupService);
    verifyNoMoreInteractions(groupPermissionService);
    verifyNoMoreInteractions(roleMembershipService);
  }

  @Test
  public void testGetGroups() throws Exception {
    when(groupService.getGroups()).thenReturn(singletonList(group));
    when(group.getName()).thenReturn("devs");
    when(userPermissionEvaluator.hasPermission(new GroupIdentity("devs"), VIEW)).thenReturn(true);
    when(group.getLabel()).thenReturn("Developers");

    mockMvc
        .perform(get(GROUP_END_POINT))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].name", is("devs")))
        .andExpect(jsonPath("$[0].label", is("Developers")));
  }

  @Test
  public void testGetGroupPermissionDenied() throws Exception {
    when(groupService.getGroups()).thenReturn(singletonList(group));
    when(group.getName()).thenReturn("devs");
    when(userPermissionEvaluator.hasPermission(new GroupIdentity("devs"), VIEW)).thenReturn(false);

    mockMvc
        .perform(get(GROUP_END_POINT))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  public void testGetMembers() throws Exception {
    when(userPermissionEvaluator.hasPermission(new GroupIdentity("devs"), VIEW_MEMBERSHIP))
        .thenReturn(true);
    when(groupService.getGroup("devs")).thenReturn(group);
    when(group.getRoles()).thenReturn(ImmutableList.of(viewer, editor, manager));

    when(roleMembershipService.getMemberships(ImmutableList.of(viewer, editor, manager)))
        .thenReturn(singletonList(memberShip));

    when(memberShip.getUser()).thenReturn(user);
    when(memberShip.getRole()).thenReturn(editor);

    when(user.getUsername()).thenReturn("henkie");
    when(user.getId()).thenReturn("userId");

    when(editor.getName()).thenReturn("DEVS_EDITOR");
    when(editor.getLabel()).thenReturn("Developers Editor");

    mockMvc
        .perform(get(GROUP_END_POINT + "/devs/member"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].user.username", is("henkie")))
        .andExpect(jsonPath("$[0].user.id", is("userId")))
        .andExpect(jsonPath("$[0].role.roleName", is("DEVS_EDITOR")))
        .andExpect(jsonPath("$[0].role.roleLabel", is("Developers Editor")));
  }

  @Test
  public void testGetMembersPermissionDenied() throws Exception {
    when(userPermissionEvaluator.hasPermission(new GroupIdentity("devs"), VIEW_MEMBERSHIP))
        .thenReturn(false);

    mockMvc
        .perform(get(GROUP_END_POINT + "/devs/member"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.errors[0].code").value("DS10"))
        .andExpect(
            jsonPath("$.errors[0].message")
                .value("No 'View Membership' permission on group 'devs'."));
  }

  @Test
  public void testGetMembersUnknownGroup() throws Exception {
    when(userPermissionEvaluator.hasPermission(new GroupIdentity("devs"), VIEW_MEMBERSHIP))
        .thenReturn(true);
    when(groupMetadata.getLabel("en")).thenReturn("Group");
    when(attribute.getLabel("en")).thenReturn("Name");
    doThrow(new UnknownEntityException(groupMetadata, attribute, "devs"))
        .when(groupService)
        .getGroup("devs");

    mockMvc
        .perform(get(GROUP_END_POINT + "/devs/member"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors[0].code").value("D02"))
        .andExpect(
            jsonPath("$.errors[0].message")
                .value("Unknown entity with 'Name' 'devs' of type 'Group'."));
  }

  @Test
  public void testAddMembership() throws Exception {
    when(userPermissionEvaluator.hasPermission(new GroupIdentity("devs"), ADD_MEMBERSHIP))
        .thenReturn(true);
    when(groupService.getGroup("devs")).thenReturn(group);
    when(roleService.getRole("DEVS_EDITOR")).thenReturn(editor);
    when(userService.getUser("henkie")).thenReturn(user);

    mockMvc
        .perform(
            post(GROUP_END_POINT + "/devs/member")
                .contentType(APPLICATION_JSON_UTF8)
                .content(gson.toJson(addGroupMember("henkie", "DEVS_EDITOR"))))
        .andExpect(status().isCreated())
        .andExpect(
            header()
                .string(
                    "Location",
                    "http://localhost" + GROUP_END_POINT + "/devs/member/devs/member/henkie"));

    verify(groupService).addMember(group, user, editor);
  }

  @Test
  public void testAddMembershipPermissionDenied() throws Exception {
    when(userPermissionEvaluator.hasPermission(new GroupIdentity("devs"), ADD_MEMBERSHIP))
        .thenReturn(false);
    mockMvc
        .perform(
            post(GROUP_END_POINT + "/devs/member")
                .contentType(APPLICATION_JSON_UTF8)
                .content(gson.toJson(addGroupMember("user", "DEVS_EDITOR"))))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.errors[0].code").value("DS10"))
        .andExpect(
            jsonPath("$.errors[0].message")
                .value("No 'Add Membership' permission on group 'devs'."));
  }

  @Test
  public void testAddMembershipUnknownGroup() throws Exception {
    when(userPermissionEvaluator.hasPermission(new GroupIdentity("devs"), ADD_MEMBERSHIP))
        .thenReturn(true);

    when(groupMetadata.getLabel("en")).thenReturn("Group");
    when(attribute.getLabel("en")).thenReturn("Name");
    doThrow(new UnknownEntityException(groupMetadata, attribute, "devs"))
        .when(groupService)
        .getGroup("devs");

    mockMvc
        .perform(
            post(GROUP_END_POINT + "/devs/member")
                .contentType(APPLICATION_JSON_UTF8)
                .content(gson.toJson(addGroupMember("user", "DEVS_EDITOR"))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors[0].code").value("D02"))
        .andExpect(
            jsonPath("$.errors[0].message")
                .value("Unknown entity with 'Name' 'devs' of type 'Group'."));
  }

  @Test
  public void testAddMembershipUnknownRole() throws Exception {
    when(userPermissionEvaluator.hasPermission(new GroupIdentity("devs"), ADD_MEMBERSHIP))
        .thenReturn(true);

    when(groupService.getGroup("devs")).thenReturn(group);
    when(roleMetadata.getLabel("en")).thenReturn("Role");
    when(attribute.getLabel("en")).thenReturn("Name");
    doThrow(new UnknownEntityException(roleMetadata, attribute, "DEVS_EDITOR"))
        .when(roleService)
        .getRole("DEVS_EDITOR");

    mockMvc
        .perform(
            post(GROUP_END_POINT + "/devs/member")
                .contentType(APPLICATION_JSON_UTF8)
                .content(gson.toJson(addGroupMember("user", "DEVS_EDITOR"))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors[0].code").value("D02"))
        .andExpect(
            jsonPath("$.errors[0].message")
                .value("Unknown entity with 'Name' 'DEVS_EDITOR' of type 'Role'."));
  }

  @Test
  public void testAddMembershipUnknownUser() throws Exception {
    when(userPermissionEvaluator.hasPermission(new GroupIdentity("devs"), ADD_MEMBERSHIP))
        .thenReturn(true);
    when(groupService.getGroup("devs")).thenReturn(group);
    when(roleService.getRole("DEVS_EDITOR")).thenReturn(editor);
    when(userMetadata.getLabel("en")).thenReturn("User");
    when(attribute.getLabel("en")).thenReturn("Name");
    doThrow(new UnknownEntityException(userMetadata, attribute, "henkie"))
        .when(userService)
        .getUser("user");

    mockMvc
        .perform(
            post(GROUP_END_POINT + "/devs/member")
                .contentType(APPLICATION_JSON_UTF8)
                .content(gson.toJson(addGroupMember("user", "DEVS_EDITOR"))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors[0].code").value("D02"))
        .andExpect(
            jsonPath("$.errors[0].message")
                .value("Unknown entity with 'Name' 'henkie' of type 'User'."));
  }

  @Test
  public void testRemoveMembership() throws Exception {
    when(groupService.getGroup("devs")).thenReturn(group);
    when(userService.getUser("henkie")).thenReturn(user);
    when(userPermissionEvaluator.hasPermission(new GroupIdentity("devs"), REMOVE_MEMBERSHIP))
        .thenReturn(true);
    mockMvc
        .perform(delete("/api/plugin/security/group/devs/member/henkie"))
        .andExpect(status().isNoContent());
    verify(groupService).removeMember(group, user);
  }

  @Test
  public void testRemoveMembershipPermissionDenied() throws Exception {
    when(userPermissionEvaluator.hasPermission(new GroupIdentity("devs"), REMOVE_MEMBERSHIP))
        .thenReturn(false);
    mockMvc
        .perform(delete("/api/plugin/security/group/devs/member/henkie"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.errors[0].code").value("DS10"))
        .andExpect(
            jsonPath("$.errors[0].message")
                .value("No 'Remove Membership' permission on group 'devs'."));
  }

  @Test
  public void testRemoveMembershipUnknownGroup() throws Exception {
    when(userPermissionEvaluator.hasPermission(new GroupIdentity("devs"), REMOVE_MEMBERSHIP))
        .thenReturn(true);
    when(groupMetadata.getLabel("en")).thenReturn("Group");
    when(attribute.getLabel("en")).thenReturn("Name");
    doThrow(new UnknownEntityException(groupMetadata, attribute, "devs"))
        .when(groupService)
        .getGroup("devs");

    mockMvc
        .perform(delete("/api/plugin/security/group/devs/member/henkie"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors[0].code").value("D02"))
        .andExpect(
            jsonPath("$.errors[0].message")
                .value("Unknown entity with 'Name' 'devs' of type 'Group'."));
  }

  @Test
  public void testRemoveMembershipUnknownUser() throws Exception {
    when(groupService.getGroup("devs")).thenReturn(group);
    when(userService.getUser("henkie"))
        .thenThrow(new UnknownEntityException(UserMetaData.USER, "henkie"));
    when(userPermissionEvaluator.hasPermission(new GroupIdentity("devs"), REMOVE_MEMBERSHIP))
        .thenReturn(true);
    mockMvc
        .perform(delete("/api/plugin/security/group/devs/member/henkie"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testRemoveMembershipNotAMember() throws Exception {
    when(groupService.getGroup("devs")).thenReturn(group);
    when(userService.getUser("henkie")).thenReturn(user);
    when(userPermissionEvaluator.hasPermission(new GroupIdentity("devs"), REMOVE_MEMBERSHIP))
        .thenReturn(true);

    when(roleMembershipMetadata.getLabel("en")).thenReturn("Role Membership");
    when(attribute.getLabel("en")).thenReturn("User");
    doThrow(new UnknownEntityException(roleMembershipMetadata, attribute, "henkie"))
        .when(groupService)
        .removeMember(group, user);

    mockMvc
        .perform(delete("/api/plugin/security/group/devs/member/henkie"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors[0].code").value("D02"))
        .andExpect(
            jsonPath("$.errors[0].message")
                .value("Unknown entity with 'User' 'henkie' of type 'Role Membership'."));
  }

  @Test
  public void testUpdateMembership() throws Exception {
    when(userPermissionEvaluator.hasPermission(new GroupIdentity("devs"), UPDATE_MEMBERSHIP))
        .thenReturn(true);
    when(groupService.getGroup("devs")).thenReturn(group);
    when(userService.getUser("henkie")).thenReturn(user);
    when(roleService.getRole("DEVS_EDITOR")).thenReturn(editor);

    mockMvc
        .perform(
            put(GROUP_END_POINT + "/devs/member/henkie")
                .content(gson.toJson(updateGroupMember("DEVS_EDITOR")))
                .contentType(APPLICATION_JSON_UTF8))
        .andExpect(status().isOk())
        .andExpect(header().doesNotExist("Location"));

    verify(groupService).updateMemberRole(group, user, editor);
  }

  @Test
  public void testUpdateMembershipUnknownGroup() throws Exception {
    when(userPermissionEvaluator.hasPermission(new GroupIdentity("devs"), UPDATE_MEMBERSHIP))
        .thenReturn(true);
    doThrow(new UnknownEntityException(groupMetadata, attribute, "devs"))
        .when(groupService)
        .getGroup("devs");
    when(groupMetadata.getLabel("en")).thenReturn("Group");
    when(attribute.getLabel("en")).thenReturn("Name");

    mockMvc
        .perform(
            put(GROUP_END_POINT + "/devs/member/henkie")
                .content(gson.toJson(updateGroupMember("DEVS_EDITOR")))
                .contentType(APPLICATION_JSON_UTF8))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors[0].code").value("D02"))
        .andExpect(
            jsonPath("$.errors[0].message")
                .value("Unknown entity with 'Name' 'devs' of type 'Group'."));
  }

  @Test
  public void testUpdateMembershipUnknownUser() throws Exception {
    when(userPermissionEvaluator.hasPermission(new GroupIdentity("devs"), UPDATE_MEMBERSHIP))
        .thenReturn(true);
    when(groupService.getGroup("devs")).thenReturn(group);

    doThrow(new UnknownEntityException(userMetadata, attribute, "henkie"))
        .when(userService)
        .getUser("henkie");
    when(userMetadata.getLabel("en")).thenReturn("User");
    when(attribute.getLabel("en")).thenReturn("Name");

    mockMvc
        .perform(
            put(GROUP_END_POINT + "/devs/member/henkie")
                .content(gson.toJson(updateGroupMember("DEVS_EDITOR")))
                .contentType(APPLICATION_JSON_UTF8))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors[0].code").value("D02"))
        .andExpect(
            jsonPath("$.errors[0].message")
                .value("Unknown entity with 'Name' 'henkie' of type 'User'."));
  }

  @Test
  public void testUpdateMembershipUnknownRole() throws Exception {
    when(userPermissionEvaluator.hasPermission(new GroupIdentity("devs"), UPDATE_MEMBERSHIP))
        .thenReturn(true);
    when(groupService.getGroup("devs")).thenReturn(group);
    when(userService.getUser("henkie")).thenReturn(user);
    doThrow(new UnknownEntityException(roleMetadata, attribute, "DEVS_EDITOR"))
        .when(roleService)
        .getRole("DEVS_EDITOR");
    when(roleMetadata.getLabel("en")).thenReturn("Role");
    when(attribute.getLabel("en")).thenReturn("Name");

    mockMvc
        .perform(
            put(GROUP_END_POINT + "/devs/member/henkie")
                .content(gson.toJson(updateGroupMember("DEVS_EDITOR")))
                .contentType(APPLICATION_JSON_UTF8))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors[0].code").value("D02"))
        .andExpect(
            jsonPath("$.errors[0].message")
                .value("Unknown entity with 'Name' 'DEVS_EDITOR' of type 'Role'."));
  }

  @Test
  public void testUpdateMembershipPermissionDenied() throws Exception {
    when(userPermissionEvaluator.hasPermission(new GroupIdentity("devs"), UPDATE_MEMBERSHIP))
        .thenReturn(false);

    mockMvc
        .perform(
            put(GROUP_END_POINT + "/devs/member/henkie")
                .contentType(APPLICATION_JSON_UTF8)
                .content(gson.toJson(updateGroupMember("DEVS_MANAGER"))))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.errors[0].code").value("DS10"))
        .andExpect(
            jsonPath("$.errors[0].message")
                .value("No 'Update Membership' permission on group 'devs'."));
  }

  @Test
  public void testUpdateMembershipNotAValidGroupRole() throws Exception {
    when(groupService.getGroup("devs")).thenReturn(group);
    when(userService.getUser("henkie")).thenReturn(user);
    when(roleService.getRole("DEVS_EDITOR")).thenReturn(editor);
    when(userPermissionEvaluator.hasPermission(new GroupIdentity("devs"), UPDATE_MEMBERSHIP))
        .thenReturn(true);

    doThrow(new NotAValidGroupRoleException(editor, group))
        .when(groupService)
        .updateMemberRole(group, user, editor);

    when(group.getName()).thenReturn("devs");
    when(editor.getName()).thenReturn("DEVS_EDITOR");

    mockMvc
        .perform(
            put(GROUP_END_POINT + "/devs/member/henkie")
                .content(gson.toJson(updateGroupMember("DEVS_EDITOR")))
                .contentType(APPLICATION_JSON_UTF8))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors[0].code").value("DS15"))
        .andExpect(
            jsonPath("$.errors[0].message")
                .value("Role 'DEVS_EDITOR' is not a valid group role for group 'devs'."));
  }

  @Test
  public void testGetGroupRoles() throws Exception {
    when(userPermissionEvaluator.hasPermission(new GroupIdentity("devs"), VIEW)).thenReturn(true);

    when(editor.getLabel()).thenReturn("role-label");
    when(editor.getName()).thenReturn("role-name");
    Iterable<Role> groupRoles = singletonList(editor);
    when(group.getRoles()).thenReturn(groupRoles);
    when(groupService.getGroup("devs")).thenReturn(group);

    mockMvc
        .perform(get(GROUP_END_POINT + "/devs/role/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].roleName", is("role-name")))
        .andExpect(jsonPath("$[0].roleLabel", is("role-label")));
  }

  @Test
  public void testGetGroupRolesPermissionDenied() throws Exception {
    when(userPermissionEvaluator.hasPermission(new GroupIdentity("devs"), VIEW)).thenReturn(false);

    mockMvc
        .perform(get(GROUP_END_POINT + "/devs/role/"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.errors[0].code").value("DS10"))
        .andExpect(jsonPath("$.errors[0].message").value("No 'View' permission on group 'devs'."));
  }

  @Test
  @WithMockUser(roles = {"MANAGER"})
  public void testGetUsers() throws Exception {
    when(user.getId()).thenReturn("id");
    when(user.getUsername()).thenReturn("name");

    User anonymousUser = mock(User.class);
    when(anonymousUser.getUsername()).thenReturn("anonymous");

    when(userService.getUsers()).thenReturn(Arrays.asList(user, anonymousUser));

    mockMvc
        .perform(get(TEMP_USER_END_POINT))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id", is("id")))
        .andExpect(jsonPath("$[0].username", is("name")));
  }

  @Test
  public void testPermissions() throws Exception {
    Set<Permission> set = new HashSet<>(Collections.singletonList(GroupPermission.ADD_MEMBERSHIP));

    when(userPermissionEvaluator.getPermissions(
            new GroupIdentity("devs"), GroupPermission.values()))
        .thenReturn(set);

    mockMvc
        .perform(get(GROUP_END_POINT + "/devs/permission"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0]", is("ADD_MEMBERSHIP")));
  }

  @Configuration
  public static class Config {
    @Bean
    public GlobalControllerExceptionHandler globalControllerExceptionHandler() {
      return new GlobalControllerExceptionHandler();
    }

    @Bean
    public FallbackExceptionHandler fallbackExceptionHandler() {
      return new FallbackExceptionHandler();
    }

    @Bean
    public SpringExceptionHandler springExceptionHandler() {
      return new SpringExceptionHandler();
    }
  }
}
