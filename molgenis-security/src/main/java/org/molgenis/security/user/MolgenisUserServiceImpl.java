package org.molgenis.security.user;

import org.molgenis.auth.*;
import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.molgenis.auth.MolgenisGroupMemberMetaData.MOLGENIS_GROUP_MEMBER;
import static org.molgenis.auth.MolgenisUserMetaData.MOLGENIS_USER;

/**
 * Manage user in groups
 */
@Service
public class MolgenisUserServiceImpl implements MolgenisUserService
{
	private final DataService dataService;

	@Autowired
	public MolgenisUserServiceImpl(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		this.dataService = dataService;
	}

	@Override
	@RunAsSystem
	public List<String> getSuEmailAddresses()
	{
		Stream<MolgenisUser> superUsers = dataService
				.findAll(MOLGENIS_USER, new QueryImpl<MolgenisUser>().eq(MolgenisUserMetaData.SUPERUSER, true),
						MolgenisUser.class);
		return superUsers.map(MolgenisUser::getEmail).collect(toList());
	}

	@Override
	@RunAsSystem
	public MolgenisUser getUser(String username)
	{
		return dataService
				.findOne(MOLGENIS_USER, new QueryImpl<MolgenisUser>().eq(MolgenisUserMetaData.USERNAME, username),
						MolgenisUser.class);
	}

	@Override
	@RunAsSystem
	public Iterable<MolgenisGroup> getUserGroups(String username)
	{
		Fetch fetch = new Fetch().field(MolgenisGroupMemberMetaData.MOLGENIS_GROUP,
				new Fetch().field(MolgenisGroupMetaData.ID).field(MolgenisGroupMetaData.NAME)
						.field(MolgenisGroupMetaData.ACTIVE));
		Stream<MolgenisGroupMember> molgenisGroupMembers = dataService
				.query(MOLGENIS_GROUP_MEMBER, MolgenisGroupMember.class).fetch(fetch)
				.eq(MolgenisGroupMemberMetaData.MOLGENIS_USER, getUser(username)).findAll();
		// N.B. Must collect the results in a list before yielding up the RunAsSystem privileges!
		return molgenisGroupMembers.map(MolgenisGroupMember::getMolgenisGroup).collect(toList());
	}

	@Override
	@RunAsSystem
	public void update(MolgenisUser user)
	{
		dataService.update(MOLGENIS_USER, user);
	}

	@Override
	@RunAsSystem
	public MolgenisUser getUserByEmail(String email)
	{
		return dataService.findOne(MOLGENIS_USER, new QueryImpl<MolgenisUser>().eq(MolgenisUserMetaData.EMAIL, email),
				MolgenisUser.class);
	}
}
