package org.molgenis.security.user;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

import org.molgenis.auth.MolgenisGroup;
import org.molgenis.auth.MolgenisGroupMember;
import org.molgenis.auth.MolgenisUser;
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
		Stream<MolgenisUser> superUsers = dataService.findAll(MolgenisUser.ENTITY_NAME,
				new QueryImpl().eq(MolgenisUser.SUPERUSER, true), MolgenisUser.class);
		return superUsers.map(MolgenisUser::getEmail).collect(toList());
	}

	@Override
	@RunAsSystem
	public MolgenisUser getUser(String username)
	{
		return dataService.findOne(MolgenisUser.ENTITY_NAME, new QueryImpl().eq(MolgenisUser.USERNAME, username),
				MolgenisUser.class);
	}

	@Override
	@RunAsSystem
	public Iterable<MolgenisGroup> getUserGroups(String username)
	{
		Stream<MolgenisGroupMember> molgenisGroupMembers = dataService.findAll(MolgenisGroupMember.ENTITY_NAME,
				new QueryImpl().eq(MolgenisGroupMember.MOLGENISUSER, getUser(username)), MolgenisGroupMember.class);
		// N.B. Must collect the results in a list before yielding up the RunAsSystem privileges!
		return molgenisGroupMembers.map(MolgenisGroupMember::getMolgenisGroup).collect(toList());
	}

	@Override
	@RunAsSystem
	public void update(MolgenisUser user)
	{
		dataService.update(MolgenisUser.ENTITY_NAME, user);
	}

	@Override
	@RunAsSystem
	public MolgenisUser getUserByEmail(String email)
	{
		return dataService.findOne(MolgenisUser.ENTITY_NAME, new QueryImpl().eq(MolgenisUser.EMAIL, email),
				MolgenisUser.class);
	}
}
