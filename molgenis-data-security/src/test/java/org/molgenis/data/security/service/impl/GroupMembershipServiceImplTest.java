package org.molgenis.data.security.service.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.mockito.*;
import org.mockito.quality.Strictness;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.security.model.GroupEntity;
import org.molgenis.data.security.model.GroupMembershipEntity;
import org.molgenis.data.security.model.GroupMembershipFactory;
import org.molgenis.data.security.model.GroupMetadata;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.model.Group;
import org.molgenis.security.core.model.GroupMembership;
import org.molgenis.security.core.model.User;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.security.model.GroupMembershipMetadata.*;
import static org.molgenis.data.security.model.GroupMetadata.PARENT;
import static org.testng.Assert.assertEquals;

public class GroupMembershipServiceImplTest
{
	@Mock
	private GroupMembershipEntity groupMembershipEntity1;
	@Mock
	private GroupMembershipEntity groupMembershipEntity2;
	@Mock
	private GroupEntity groupEntity1;
	@Mock
	private GroupEntity groupEntity2;
	@Mock
	private GroupMembership groupMembership1;
	@Mock
	private GroupMembership groupMembership2;
	@Mock
	private GroupMembership groupMembership3;
	@Mock
	private User user;
	@Mock
	private GroupMembershipFactory groupMembershipFactory;
	@Mock
	private DataService dataService;
	@Captor
	private ArgumentCaptor<Stream<Object>> idCaptor;
	@Captor
	private ArgumentCaptor<Stream<GroupMembershipEntity>> entityCaptor;
	@InjectMocks
	private GroupMembershipServiceImpl groupMembershipService;
	private MockitoSession mockitoSession;

	@BeforeMethod
	public void beforeMethod()
	{
		groupMembershipService = null;
		mockitoSession = mockitoSession().strictness(Strictness.STRICT_STUBS).initMocks(this).startMocking();
	}

	@AfterMethod
	public void afterMethod()
	{
		mockitoSession.finishMocking();
	}

	@Test
	public void testAdd()
	{
		when(groupMembershipFactory.create()).thenReturn(groupMembershipEntity1, groupMembershipEntity2);
		when(groupMembershipEntity1.updateFrom(groupMembership1)).thenReturn(groupMembershipEntity1);
		when(groupMembershipEntity2.updateFrom(groupMembership2)).thenReturn(groupMembershipEntity2);

		groupMembershipService.add(ImmutableList.of(groupMembership1, groupMembership2));

		verify(dataService).add(eq(GROUP_MEMBERSHIP), entityCaptor.capture());
		Set<Object> addedEntities = entityCaptor.getValue().collect(Collectors.toSet());
		assertEquals(addedEntities, ImmutableSet.of(groupMembershipEntity1, groupMembershipEntity2));
	}

	@Test
	public void testDelete()
	{
		when(groupMembership1.getId()).thenReturn(Optional.of("id1"));
		when(groupMembership2.getId()).thenReturn(Optional.empty());
		when(groupMembership3.getId()).thenReturn(Optional.of("id2"));

		groupMembershipService.delete(ImmutableList.of(groupMembership1, groupMembership2, groupMembership3));

		verify(dataService).deleteAll(eq(GROUP_MEMBERSHIP), idCaptor.capture());
		Set<Object> deletedIds = idCaptor.getValue().collect(Collectors.toSet());
		assertEquals(deletedIds, ImmutableSet.of("id1", "id2"));
	}

	@Test
	public void testGetGroupMembershipsForUser()
	{
		String userId = "abcde";
		when(user.getId()).thenReturn(Optional.of(userId));
		Query<GroupMembershipEntity> forUser = new QueryImpl<GroupMembershipEntity>().eq(USER, userId);
		forUser.sort().on(START);
		when(dataService.findAll(GROUP_MEMBERSHIP, forUser, GroupMembershipEntity.class)).thenReturn(
				Stream.of(groupMembershipEntity1, groupMembershipEntity2));
		when(groupMembershipEntity1.toGroupMembership()).thenReturn(groupMembership1);
		when(groupMembershipEntity2.toGroupMembership()).thenReturn(groupMembership2);

		assertEquals(groupMembershipService.getGroupMemberships(user),
				ImmutableList.of(groupMembership1, groupMembership2));
	}

	@Test
	public void testGetGroupMembershipsForGroup()
	{
		Group parent = mock(Group.class);
		when(parent.getId()).thenReturn(Optional.of("parent"));
		Group child1 = mock(Group.class);
		when(child1.getId()).thenReturn(Optional.of("child1"));
		Group child2 = mock(Group.class);
		when(child2.getId()).thenReturn(Optional.of("child2"));

		Query<GroupEntity> withParent = new QueryImpl<GroupEntity>().eq(PARENT, "parent");
		doReturn(Stream.of(groupEntity1, groupEntity2)).when(dataService)
													   .findAll(GroupMetadata.GROUP, withParent, GroupEntity.class);

		when(groupEntity1.toGroup()).thenReturn(child1);
		when(groupEntity2.toGroup()).thenReturn(child2);

		Query<GroupMembershipEntity> forGroups = new QueryImpl<GroupMembershipEntity>().in(GROUP,
				asList("parent", "child1", "child2"));
		forGroups.sort().on(START);

		doReturn(Stream.of(groupMembershipEntity1, groupMembershipEntity2)).when(dataService)
																		   .findAll(GROUP_MEMBERSHIP, forGroups,
																				   GroupMembershipEntity.class);
		when(groupMembershipEntity1.toGroupMembership()).thenReturn(groupMembership1);
		when(groupMembershipEntity2.toGroupMembership()).thenReturn(groupMembership2);

		assertEquals(groupMembershipService.getGroupMemberships(parent),
				ImmutableList.of(groupMembership1, groupMembership2));
	}
}