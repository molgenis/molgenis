package org.molgenis.security.user;

import static java.util.stream.Collectors.toList;
import static org.molgenis.auth.MolgenisUserMetaData.MOLGENIS_USER;

import java.util.List;
import java.util.stream.Stream;

import org.molgenis.auth.MolgenisGroup;
import org.molgenis.auth.MolgenisGroupMember;
import org.molgenis.auth.MolgenisGroupMemberMetaData;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.MolgenisUserMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
		Stream<MolgenisGroupMember> molgenisGroupMembers = dataService
				.findAll(MolgenisGroupMemberMetaData.MOLGENIS_GROUP_MEMBER, new QueryImpl<MolgenisGroupMember>()
						.eq(MolgenisGroupMemberMetaData.MOLGENIS_USER, getUser(username)), MolgenisGroupMember.class);
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
