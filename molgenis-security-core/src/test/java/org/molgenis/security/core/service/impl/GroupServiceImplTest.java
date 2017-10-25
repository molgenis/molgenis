package org.molgenis.security.core.service.impl;

import com.google.common.collect.ImmutableList;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoSession;
import org.molgenis.security.core.model.Group;
import org.molgenis.security.core.model.GroupMembership;
import org.molgenis.security.core.model.User;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.util.Collections.singleton;
import static org.mockito.Mockito.mockitoSession;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.STRICT_STUBS;
import static org.testng.Assert.assertEquals;

public class GroupServiceImplTest
{
	@Mock
	private User user;
	@Mock
	private GroupMembership groupMembership1;
	@Mock
	private GroupMembership groupMembership2;
	@Mock
	private GroupMembership groupMembership3;
	@Mock
	private Group group1;
	@Mock
	private Group group2;
	@Mock
	private GroupMembershipService groupMembershipService;

	@InjectMocks
	private GroupServiceImpl groupService;

	private MockitoSession mockitoSession;

	@BeforeMethod
	public void beforeMethod()
	{
		groupService = null;
		mockitoSession = mockitoSession().strictness(STRICT_STUBS).initMocks(this).startMocking();
	}

	@AfterMethod
	public void afterMethod()
	{
		mockitoSession.finishMocking();
	}

	@Test
	public void testGetCurrentGroups()
	{
		when(groupMembershipService.getGroupMemberships(user)).thenReturn(
				ImmutableList.of(groupMembership1, groupMembership2, groupMembership3));

		when(groupMembership1.isCurrent()).thenReturn(true);
		when(groupMembership2.isCurrent()).thenReturn(false);
		when(groupMembership3.isCurrent()).thenReturn(true);

		when(groupMembership1.getGroup()).thenReturn(group1);
		when(groupMembership3.getGroup()).thenReturn(group1);

		assertEquals(groupService.getCurrentGroups(user), singleton(group1));
	}

}