package org.molgenis.security.group;

import com.google.common.collect.ImmutableSet;
import org.mockito.Mock;
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

import static org.mockito.Mockito.verify;

@ContextConfiguration(classes = GroupRestControllerTest.Config.class)
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class GroupRestControllerTest extends AbstractMockitoTestNGSpringContextTests
{
	private final GroupValueFactory groupValueFactory = new GroupValueFactory();
	@Mock
	private GroupService groupService;
	@Mock
	private RoleMembershipService roleMembershipService;

	private GroupRestController groupRestController;

	@BeforeMethod
	public void beforeMethod()
	{
		groupRestController = new GroupRestController(groupValueFactory, groupService, roleMembershipService);
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

	@Configuration
	public static class Config
	{
	}
}