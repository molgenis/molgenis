package org.molgenis.data.security.service.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoSession;
import org.molgenis.data.DataService;
import org.molgenis.data.security.model.GroupEntity;
import org.molgenis.data.security.model.GroupFactory;
import org.molgenis.data.security.model.RoleFactory;
import org.molgenis.security.core.model.*;
import org.molgenis.security.core.service.impl.GroupMembershipService;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.Month.JANUARY;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.*;
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
	private Group group3;
	@Mock
	private Group group4;
	@Mock
	private GroupMembershipService groupMembershipService;
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private GroupFactory groupFactory;
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private RoleFactory roleFactory;
	@Mock
	private DataService dataService;

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
	public void testAddGroupMembershipJoiningOverlaps()
	{
		/*
			Going to add user to group1.
			user already is a member of group2, group3, and group4
			           [------group1-----)
			[---group1---)  [group1)  [---group1---------

			Desired outcome:
			[-----------------group1---------------------
		 */
		GroupMembership m1 = createMembership(null, 2, 5, group1);
		GroupMembership m2 = createMembership("m2", 1, 3, group1);
		GroupMembership m3 = createMembership("m3", 3, 4, group1);
		GroupMembership m4 = createMembership("m4", 4, null, group1);
		when(group1.hasSameParentAs(any(Group.class))).thenReturn(true);
		when(groupMembershipService.getGroupMemberships(user)).thenReturn(ImmutableList.of(m2, m3, m4));

		groupService.addUserToGroup(m1.getUser(), m1.getGroup(), m1.getStart(), m1.getEnd().get());

		GroupMembership allCombined = createMembership(null, 1, null, group1);
		verify(groupMembershipService).delete(ImmutableList.of(m2, m3, m4));
		verify(groupMembershipService).add(ImmutableList.of());
		verify(groupMembershipService).add(ImmutableList.of(allCombined));
	}

	@Test
	public void testAddGroupMembershipConflictingOverlaps()
	{
		/*
			Going to add user to group1.
			user already is a member of group2, group3, and group4
			           [------group1-----)
			[---group2---)[--group3--)[---group4---------

			Desired outcome:
			[---group2)[------group1-----)[-group4-------
		 */
		GroupMembership m1 = createMembership(null, 2, 5, group1);
		GroupMembership m2 = createMembership("m2", 1, 3, group2);
		GroupMembership m3 = createMembership("m3", 3, 4, group3);
		GroupMembership m4 = createMembership("m4", 4, null, group4);
		when(group1.hasSameParentAs(any(Group.class))).thenReturn(true);
		when(groupMembershipService.getGroupMemberships(user)).thenReturn(ImmutableList.of(m2, m3, m4));

		groupService.addUserToGroup(m1.getUser(), m1.getGroup(), m1.getStart(), m1.getEnd().get());

		GroupMembership m2Truncated = createMembership(null, 1, 2, group2);
		GroupMembership m4Truncated = createMembership(null, 5, null, group4);
		verify(groupMembershipService).delete(ImmutableList.of(m2, m3, m4));
		verify(groupMembershipService).add(ImmutableList.of(m2Truncated, m4Truncated));
		verify(groupMembershipService).add(ImmutableList.of(m1));
	}

	private GroupMembership createMembership(String id, int start, Integer end, Group group)
	{
		GroupMembership.Builder result = GroupMembership.builder().user(user).group(group).start(january(start));
		Optional.ofNullable(id).ifPresent(result::id);
		Optional.ofNullable(end).map(this::january).ifPresent(result::end);
		return result.build();
	}

	private Instant january(int day)
	{
		return LocalDate.of(2017, JANUARY, day).atStartOfDay(UTC).toInstant();
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

	@Test
	public void testGetGroupMembershipsForGroup()
	{
		when(groupMembershipService.getGroupMemberships(group1)).thenReturn(
				ImmutableList.of(groupMembership1, groupMembership2, groupMembership3));

		assertEquals(groupService.getGroupMemberships(group1),
				ImmutableList.of(groupMembership1, groupMembership2, groupMembership3));
	}

	@Test
	public void testGetGroupMembershipsForUser()
	{
		when(groupMembershipService.getGroupMemberships(user)).thenReturn(
				ImmutableList.of(groupMembership1, groupMembership2, groupMembership3));

		assertEquals(groupService.getGroupMemberships(user),
				ImmutableList.of(groupMembership1, groupMembership2, groupMembership3));
	}

	@Test
	public void testCreateGroup()
	{
		String label = "BBMRI_NL";
		List<Role> roles = Lists.newArrayList(
				Role.builder().id("abab").label(label + ConceptualRoles.GROUPADMIN.name()).build());
		Group group = Group.builder().label(label).roles(roles).build();

		GroupEntity groupParentEntity = mock(GroupEntity.class);
		when(groupParentEntity.toGroup()).thenReturn(group);
		when(groupFactory.create().updateFrom(group, groupFactory, roleFactory)).thenReturn(groupParentEntity);

		Group createdGroup = groupService.createGroup(group);
		assertEquals(createdGroup.getLabel(), label);
	}

}