package org.molgenis.security.group;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.*;
import org.molgenis.data.security.permission.RoleMembershipService;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.core.GroupValueFactory;
import org.molgenis.security.core.model.GroupValue;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.molgenis.web.converter.GsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import static org.molgenis.security.group.GroupRestController.GROUP_END_POINT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = { GsonConfig.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class GroupRestControllerTest extends AbstractMockitoTestNGSpringContextTests
{
	private final GroupValueFactory groupValueFactory = new GroupValueFactory();
	@Mock
	private GroupService groupService;
	@Mock
	private RoleMembershipService roleMembershipService;
	@Mock
	private DataService dataService;

	@Mock
	private RoleService roleService;

	@Mock
	private UserService userService;

	private MockMvc mockMvc;

	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	@BeforeMethod
	public void beforeMethod()
	{
		GroupRestController groupRestController = new GroupRestController(groupValueFactory, groupService,
				roleMembershipService, dataService, roleService, userService);
		mockMvc = MockMvcBuilders.standaloneSetup(groupRestController)
								 .setMessageConverters(new FormHttpMessageConverter(), gsonHttpMessageConverter)
								 .build();
	}

	@Test
	@WithMockUser
	public void testCreateGroup() throws Exception
	{
		GroupValue groupValue = groupValueFactory.createGroup("name", "Label", null, true,
				ImmutableSet.of("Manager", "Editor", "Viewer"));

		GroupCommand groupCommand = GroupCommand.create("name", "Label");

		mockMvc.perform(post(GROUP_END_POINT).contentType(MediaType.APPLICATION_JSON_UTF8)
														  .content(new Gson().toJson(groupCommand)))
			   .andExpect(status().isCreated());

		verify(groupService).persist(groupValue);
		verify(groupService).grantPermissions(groupValue);
		verify(roleMembershipService).addUserToRole("user", "NAME_MANAGER");
	}

	@Test
	@WithMockUser
	public void testGetGroup() throws Exception
	{
		Group group = mock(Group.class);
		final String groupName = "group-name";
		when(group.getName()).thenReturn(groupName);
		final String groupLabel = "group-label";
		when(group.getLabel()).thenReturn(groupLabel);
		when(dataService.findAll(GroupMetadata.GROUP, Group.class)).thenReturn(Stream.of(group));

		mockMvc.perform(get(GROUP_END_POINT))
			   .andExpect(status().isOk())
			   .andExpect(jsonPath("$", hasSize(1)))
			   .andExpect(jsonPath("$[0].name", is(groupName)))
			   .andExpect(jsonPath("$[0].label", is(groupLabel)));
	}

	@Test
	@WithMockUser
	public void testGetMembers() throws Exception
	{
		String groupName = "developers";

		Group group = mock(Group.class);
		Role managerRole = mock(Role.class);
		Iterable<Role> roles = Collections.singletonList(managerRole);
		when(group.getRoles()).thenReturn(roles);

		User user = mock(User.class);
		when(user.getUsername()).thenReturn("user-1");
		when(user.getId()).thenReturn("user-id-1");
		Role role = mockRole("role-name-a", "role label a");

		RoleMembership memberShip = mock(RoleMembership.class);
		when(memberShip.getUser()).thenReturn(user);
		when(memberShip.getRole()).thenReturn(role);
		Collection<RoleMembership> groupMemberShips = Collections.singletonList(memberShip);

		when(roleMembershipService.getMemberships(Lists.newArrayList(roles))).thenReturn(groupMemberShips);
		when(groupService.getGroup(groupName)).thenReturn(group);

		mockMvc.perform(get(GROUP_END_POINT+ "/" + groupName + "/member"))
			   .andExpect(status().isOk())
			   .andExpect(jsonPath("$", hasSize(1)))
			   .andExpect(jsonPath("$[0].user.username", is("user-1")))
			   .andExpect(jsonPath("$[0].user.id", is("user-id-1")))
			   .andExpect(jsonPath("$[0].role.roleName", is("role-name-a")))
			   .andExpect(jsonPath("$[0].role.roleLabel", is("role label a")));
	}

	@Test
	@WithMockUser
	public void testAddMember() throws Exception
	{
		String groupName = "developers";

		String newMemberName = "new-member-name";
		String newMemberRole = "new-menber-role";
		AddGroupMemberCommand addGroupMemberCommand = AddGroupMemberCommand.create(newMemberName, newMemberRole);

		mockMvc.perform(post(GROUP_END_POINT+ "/" + groupName + "/member").content(
				new Gson().toJson(addGroupMemberCommand)).contentType(MediaType.APPLICATION_JSON_UTF8))
			   .andExpect(status().isCreated());

		verify(groupService).addMember(any(), any(), any());
	}

	@Test
	@WithMockUser
	public void testRemoveMember() throws Exception
	{
		String groupName = "developers";
		String memberName = "member-1";

		mockMvc.perform(delete(GROUP_END_POINT+ "/" + groupName + "/member/" + memberName))
			   .andExpect(status().isNoContent());

		verify(groupService).removeMember(any(), any());
	}

	@Test
	@WithMockUser
	public void testUpdateMember() throws Exception
	{
		String groupName = "developers";
		String memberName = "member-1";
		String newRoleName = "new-role-name";

		UpdateGroupMemberCommand updateGroupMemberCommand = UpdateGroupMemberCommand.create(newRoleName);

		mockMvc.perform(put(GROUP_END_POINT+ "/" + groupName + "/member/" + memberName).content(
				new Gson().toJson(updateGroupMemberCommand)).contentType(MediaType.APPLICATION_JSON_UTF8))
			   .andExpect(status().isCreated());

		verify(groupService).updateMemberRole(any(), any(), any());
	}

	@Test
	@WithMockUser
	public void testGetGroupRoles() throws Exception
	{
		String groupName = "developers";

		Group group = mock(Group.class);
		final String roleName = "role-name";
		final String roleLabel = "role-label";
		Role role = mockRole(roleName, roleLabel);
		Iterable<Role> groupRoles = Collections.singletonList(role);
		when(group.getRoles()).thenReturn(groupRoles);
		when(groupService.getGroup(groupName)).thenReturn(group);

		mockMvc.perform(get(GROUP_END_POINT + "/" + groupName + "/role/"))
			   .andExpect(status().isOk())
			   .andExpect(jsonPath("$", hasSize(1)))
			   .andExpect(jsonPath("$[0].roleName", is(roleName)))
			   .andExpect(jsonPath("$[0].roleLabel", is(roleLabel)));

		verify(groupService).getGroup(groupName);
	}

	@Test
	@WithMockUser
	public void testUsers() throws Exception
	{
		String userId = "user-id";
		String username = "user-name";
		User user = mockUser(userId, username);
		Stream<User> users = Stream.of(user);
		when(dataService.findAll(UserMetaData.USER, User.class)).thenReturn(users);

		mockMvc.perform(get(GroupRestController.TEMP_USER_END_POINT))
			   .andExpect(status().isOk())
			   .andExpect(jsonPath("$", hasSize(1)))
			   .andExpect(jsonPath("$[0].id", is(userId)))
			   .andExpect(jsonPath("$[0].username", is(username)));


	}

	private Role mockRole(String roleName, String roleLabel) {
		Role role = mock(Role.class);
		when(role.getLabel()).thenReturn(roleLabel);
		when(role.getName()).thenReturn(roleName);
		return role;
	}

	private User mockUser(String id, String username) {
		User user = mock(User.class);
		when(user.getId()).thenReturn(id);
		when(user.getUsername()).thenReturn(username);
		return user;
	}
}
