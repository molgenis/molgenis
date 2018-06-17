package org.molgenis.security.group;

import com.google.common.collect.ImmutableSet;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.Group;
import org.molgenis.data.security.auth.GroupMetadata;
import org.molgenis.data.security.auth.GroupService;
import org.molgenis.data.security.permission.RoleMembershipService;
import org.molgenis.security.core.GroupValueFactory;
import org.molgenis.security.core.model.GroupValue;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = GroupRestControllerTest.Config.class)
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

	private GroupRestController groupRestController;


	@BeforeMethod
	public void beforeMethod()
	{
		groupRestController = new GroupRestController(groupValueFactory, groupService, roleMembershipService, dataService);
	}

	@Test
	@WithMockUser
	public void testCreateGroup()
	{
		GroupValue groupValue = groupValueFactory.createGroup("name", "Label", "Description of the group", true,
				ImmutableSet.of("Manager", "Editor", "Viewer"));

		groupRestController.createGroup("name", "Label", "Description of the group", true);

		verify(groupService).persist(groupValue);
		verify(groupService).grantPermissions(groupValue);
		verify(roleMembershipService).addUserToRole("user", "NAME_MANAGER");
	}

	@Test
	public void testGetGroup()
	{
		Group redGroup = mock(Group.class);
		Group greenGroup = mock(Group.class);
		when(dataService.findAll(GroupMetadata.GROUP, Group.class)).thenReturn(Stream.of(redGroup, greenGroup));
		List<GroupResponse> groups = groupRestController.getGroups();
		assertEquals(groups.size(), 2);
	}

	@Configuration
	public static class Config
	{
	}
}
