package org.molgenis.data.security.service.impl;

import com.google.common.collect.ImmutableList;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.security.model.GroupMembershipEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.model.GroupMembership;
import org.molgenis.security.core.model.User;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static org.mockito.Mockito.mockitoSession;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.model.GroupMembershipMetadata.GROUP_MEMBERSHIP;
import static org.molgenis.data.security.model.GroupMembershipMetadata.USER;
import static org.testng.Assert.assertEquals;

public class GroupMembershipServiceImplTest
{
	@Mock
	private GroupMembershipEntity groupMembershipEntity1;
	@Mock
	private GroupMembershipEntity groupMembershipEntity2;
	@Mock
	private GroupMembership groupMembership1;
	@Mock
	private GroupMembership groupMembership2;
	@Mock
	private User user;
	@Mock
	private DataService dataService;
	@InjectMocks
	private GroupMembershipServiceImpl groupService;
	private MockitoSession mockitoSession;

	@BeforeMethod
	public void beforeMethod()
	{
		groupService = null;
		mockitoSession = mockitoSession().strictness(Strictness.STRICT_STUBS).initMocks(this).startMocking();
	}

	@AfterMethod
	public void afterMethod()
	{
		mockitoSession.finishMocking();
	}

	@Test
	public void testGetGroupMemberships()
	{
		String userId = "abcde";
		when(user.getId()).thenReturn(userId);
		Query<GroupMembershipEntity> forUser = new QueryImpl<GroupMembershipEntity>().eq(USER, userId);
		when(dataService.findAll(GROUP_MEMBERSHIP, forUser, GroupMembershipEntity.class)).thenReturn(
				Stream.of(groupMembershipEntity1, groupMembershipEntity2));
		when(groupMembershipEntity1.toGroupMembership()).thenReturn(groupMembership1);
		when(groupMembershipEntity2.toGroupMembership()).thenReturn(groupMembership2);

		assertEquals(groupService.getGroupMemberships(user), ImmutableList.of(groupMembership1, groupMembership2));
	}
}