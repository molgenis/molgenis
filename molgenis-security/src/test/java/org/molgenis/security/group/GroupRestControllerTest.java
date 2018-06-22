package org.molgenis.security.group;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.Group;
import org.molgenis.data.security.auth.GroupMetadata;
import org.molgenis.data.security.auth.GroupService;
import org.molgenis.data.security.permission.RoleMembershipService;
import org.molgenis.security.core.GroupValueFactory;
import org.molgenis.security.core.model.GroupValue;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.molgenis.web.converter.GsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
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

import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import static org.molgenis.security.group.GroupRestController.GROUP_END_POINT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = { GroupRestControllerTest.Config.class, GsonConfig.class })
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

	private MockMvc mockMvc;

	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	@BeforeMethod
	public void beforeMethod()
	{
		GroupRestController groupRestController = new GroupRestController(groupValueFactory, groupService, roleMembershipService,
				dataService);
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

		GroupCommand groupCommand = new GroupCommand("name", "Label");

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
		Group redGroup = mock(Group.class);
		Group greenGroup = mock(Group.class);
		when(dataService.findAll(GroupMetadata.GROUP, Group.class)).thenReturn(Stream.of(redGroup, greenGroup));

		mockMvc.perform(get(GROUP_END_POINT))
			   .andExpect(status().isOk())
			   .andExpect(jsonPath("$", hasSize(2)));
	}

	@Configuration
	public static class Config
	{
	}
}
