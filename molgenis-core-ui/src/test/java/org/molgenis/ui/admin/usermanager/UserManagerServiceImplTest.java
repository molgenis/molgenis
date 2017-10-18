package org.molgenis.ui.admin.usermanager;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.molgenis.security.core.model.Group;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.GroupService;
import org.molgenis.security.core.service.UserService;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

import static java.util.Collections.singleton;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.STRICT_STUBS;
import static org.testng.Assert.assertEquals;

public class UserManagerServiceImplTest
{
	@InjectMocks
	private UserManagerServiceImpl userManagerService;
	@Mock
	private UserService userService;
	@Mock
	private GroupService groupService;
	@Mock
	private User user0;
	@Mock
	private User user1;
	@Mock
	private Group group0;
	@Mock
	private Group group1;

	private MockitoSession mockito;

	@BeforeMethod
	public void setup()
	{
		userManagerService = null;
		mockito = Mockito.mockitoSession().initMocks(this).strictness(STRICT_STUBS).startMocking();
	}

	@AfterMethod
	public void tearDown()
	{
		mockito.finishMocking();
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void userManagerServiceImpl()
	{
		new UserManagerServiceImpl(null, null);
	}

	@Test
	public void getAllMolgenisUsers()
	{
		when(userService.getAllUsers()).thenReturn(Arrays.asList(user0, user1));
		doReturn(singleton(group0)).when(groupService).getCurrentGroups(user0);
		doReturn(singleton(group1)).when(groupService).getCurrentGroups(user1);

		assertEquals(userManagerService.getAllUsers(), Arrays.asList(UserViewData.create(user0, singleton(group0)),
				UserViewData.create(user1, singleton(group1))));
	}
}
