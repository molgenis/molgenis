package org.molgenis.data.security.user;

import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.security.auth.*;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.molgenis.data.security.auth.GroupMemberMetaData.GROUP_MEMBER;
import static org.molgenis.data.security.auth.UserMetaData.USER;

/**
 * Manage user in groups
 */
@Service
public class UserServiceImpl implements UserService
{
	private final DataService dataService;

	public UserServiceImpl(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		this.dataService = dataService;
	}

	@Override
	@RunAsSystem
	public List<String> getSuEmailAddresses()
	{
		Stream<User> superUsers = dataService.findAll(USER, new QueryImpl<User>().eq(UserMetaData.SUPERUSER, true),
				User.class);
		return superUsers.map(User::getEmail).collect(toList());
	}

	@Override
	@RunAsSystem
	public User getUser(String username)
	{
		return dataService.findOne(USER, new QueryImpl<User>().eq(UserMetaData.USERNAME, username), User.class);
	}

	@Override
	@RunAsSystem
	public Iterable<Group> getUserGroups(String username)
	{
		Fetch fetch = new Fetch().field(GroupMemberMetaData.GROUP,
				new Fetch().field(GroupMetaData.ID).field(GroupMetaData.NAME).field(GroupMetaData.ACTIVE));
		Stream<GroupMember> molgenisGroupMembers = dataService.query(GROUP_MEMBER, GroupMember.class)
															  .fetch(fetch)
															  .eq(GroupMemberMetaData.USER, getUser(username))
															  .findAll();
		// N.B. Must collect the results in a list before yielding up the RunAsSystem privileges!
		return molgenisGroupMembers.map(GroupMember::getGroup).collect(toList());
	}

	@Override
	@RunAsSystem
	public void update(User user)
	{
		dataService.update(USER, user);
	}

	@Override
	@RunAsSystem
	public User getUserByEmail(String email)
	{
		return dataService.findOne(USER, new QueryImpl<User>().eq(UserMetaData.EMAIL, email), User.class);
	}
}
