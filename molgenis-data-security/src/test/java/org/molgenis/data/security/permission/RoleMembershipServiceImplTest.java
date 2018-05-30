package org.molgenis.data.security.permission;

import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.security.auth.*;
import org.molgenis.data.security.user.UserService;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;

import static org.mockito.Mockito.*;
import static org.molgenis.data.security.auth.RoleMembershipMetadata.ROLE_MEMBERSHIP;
import static org.molgenis.data.security.auth.UserMetaData.USERNAME;
import static org.testng.Assert.assertTrue;

public class RoleMembershipServiceImplTest extends AbstractMockitoTest
{
	@Mock
	private UserService userService;

	@Mock
	private RoleMembershipFactory roleMembershipFactory;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private DataService dataService;

	@Mock
	private UserMetaData userMetaData;

	@Mock
	private RoleMetadata roleMetadata;

	@Captor
	private ArgumentCaptor<Instant> instantCaptor;

	private RoleMembershipService roleMembershipService;

	@BeforeMethod
	public void beforeMethod()
	{
		roleMembershipService = new RoleMembershipServiceImpl(userService, roleMembershipFactory, dataService,
				userMetaData, roleMetadata);
	}

	@Test
	public void addUserToRole()
	{
		String username = "henk";
		User user = mock(User.class);
		when(userService.getUser(username)).thenReturn(user);

		Role role = mock(Role.class);
		String rolename = "GCC_MANAGER";
		when(dataService.query(RoleMetadata.ROLE, Role.class).eq(RoleMetadata.NAME, rolename).findOne()).thenReturn(
				role);

		RoleMembership roleMembership = mock(RoleMembership.class);
		when(roleMembershipFactory.create()).thenReturn(roleMembership);

		roleMembershipService.addUserToRole(username, rolename);

		verify(dataService, times(1)).add(eq(ROLE_MEMBERSHIP), any(RoleMembership.class));
		verify(roleMembership).setRole(role);
		verify(roleMembership).setUser(user);
		verify(roleMembership).setFrom(instantCaptor.capture());

		assertTrue(Duration.between(Instant.now(), instantCaptor.getValue()).getSeconds() < 1);
	}

	@Test(expectedExceptions = UnknownEntityException.class, expectedExceptionsMessageRegExp = "type:sys_sec_Role id:GCC_DELETER attribute:name")
	public void addUserToNonExistingRole()
	{
		Attribute roleNameAttr = mock(Attribute.class);
		when(roleNameAttr.getName()).thenReturn("name");
		when(userMetaData.getAttribute(USERNAME)).thenReturn(roleNameAttr);
		when(userMetaData.getId()).thenReturn("sys_sec_Role");

		String username = "henk";
		User user = mock(User.class);
		when(userService.getUser(username)).thenReturn(user);

		String rolename = "GCC_DELETER";
		when(dataService.query(RoleMetadata.ROLE, Role.class).eq(RoleMetadata.NAME, rolename).findOne()).thenReturn(
				null);

		roleMembershipService.addUserToRole(username, rolename);
	}

	@Test(expectedExceptions = UnknownEntityException.class, expectedExceptionsMessageRegExp = "type:sys_sec_User id:henk attribute:username")
	public void addNonExistingUserToRole()
	{
		Attribute userNameAttr = mock(Attribute.class);
		when(userNameAttr.getName()).thenReturn("username");
		when(userMetaData.getAttribute(USERNAME)).thenReturn(userNameAttr);
		when(userMetaData.getId()).thenReturn("sys_sec_User");

		String username = "henk";
		when(userService.getUser(username)).thenReturn(null);

		roleMembershipService.addUserToRole(username, "GCC_MANAGER");
	}
}