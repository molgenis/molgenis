package org.molgenis.security.group;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import net.dongliu.gson.GsonJava8TypeAdapterFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import org.molgenis.security.core.model.*;
import org.molgenis.security.core.service.GroupService;
import org.molgenis.security.core.service.RoleService;
import org.molgenis.security.core.service.UserService;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class GroupControllerTest
{
	@Mock
	private GroupService groupService;
	@Mock
	private RoleService roleService;
	@Mock
	private UserService userService;
	@InjectMocks
	private GroupController groupController;
	private MockMvc mockMvc;
	private MockitoSession mockitoSession;

	@BeforeMethod
	public void setUp() throws Exception
	{
		groupController = null;
		mockitoSession = Mockito.mockitoSession().strictness(Strictness.STRICT_STUBS).initMocks(this).startMocking();
		GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapterFactory(new GsonJava8TypeAdapterFactory());
		gsonHttpMessageConverter.setGson(gsonBuilder.create());
		mockMvc = MockMvcBuilders.standaloneSetup(groupController)
								 .setMessageConverters(gsonHttpMessageConverter)
								 .build();
	}

	@AfterMethod
	public void tearDown() throws Exception
	{
		mockitoSession.finishMocking();
	}

	@Test
	public void testCreateGroup() throws Exception
	{
		List<Role> roles = Lists.newArrayList(
				Role.builder().id("abcde").label(ConceptualRoles.GROUPADMIN.name()).build());
		when(roleService.createRolesForGroup("BBMRI-NL")).thenReturn(roles);
		Group group = Group.builder().id("abcde").label("BBMRI-NL").roles(roles).build();
		when(groupService.createGroup(group)).thenReturn(group);

		mockMvc.perform(post("/group/").param("label", "BBMRI-NL"))
			   .andExpect(status().isCreated())
			   .andExpect(header().string("Location", "http://localhost/group/abcde"));
	}

	@Test
	public void testGetGroupMembers() throws Exception
	{
		Role reader = Role.builder().id("kkkkk").label("BBMRI-NL Reader").build();
		Role admin = Role.builder().id("kkkkk").label("BBMRI-NL Admin").build();
		Group parent = Group.builder().id("abcde").label("BBMRI-NL").roles(emptyList()).build();
		Group readers = Group.builder().id("abcdf").label("BBMRI-NL Readers").roles(singletonList(reader)).build();
		Group admins = Group.builder().id("abcdg").label("BBMRI-NL Admins").roles(singletonList(admin)).build();
		User user = User.builder()
						.id("sadf")
						.username("admin")
						.password("abcde")
						.email("admin@example.com")
						.twoFactorAuthentication(false)
						.changePassword(false)
						.active(true)
						.build();
		User user2 = User.builder()
						 .id("asdf")
						 .username("jansenj")
						 .password("abcde")
						 .email("jansenj@example.com")
						 .twoFactorAuthentication(true)
						 .changePassword(false)
						 .active(false)
						 .build();

		Instant now = Instant.now();
		Instant yesterday = now.minus(1, DAYS);
		GroupMembership readerMembership = GroupMembership.builder().group(readers).user(user2).start(now).build();
		GroupMembership adminMembership = GroupMembership.builder().group(admins).user(user).start(yesterday).build();

		when(groupService.findGroupById("abcde")).thenReturn(Optional.of(parent));
		when(groupService.getGroupMemberships(parent)).thenReturn(ImmutableList.of(readerMembership, adminMembership));

		String jsonContent = "[{'user': {'id': 'sadf', 'username': 'admin', 'active': true}, "
				+ "'group': {'id': 'abcdg', 'label':'BBMRI-NL Admins'}," + "'start': '" + yesterday + "'},"
				+ "{'user': {'id': 'asdf', 'username': 'jansenj', 'active': false}, "
				+ "'group': {'id': 'abcdf', 'label':'BBMRI-NL Readers'}, 'start': '" + now + "'}]";
		System.out.println(jsonContent);
		mockMvc.perform(get("/group/abcde/members").header("Accept", "application/json"))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().json(jsonContent));
	}

}