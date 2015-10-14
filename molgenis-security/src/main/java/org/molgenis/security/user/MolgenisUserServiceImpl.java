package org.molgenis.security.user;

import java.util.Collections;
import java.util.List;

import org.molgenis.auth.MolgenisGroup;
import org.molgenis.auth.MolgenisGroupMember;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

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
		Iterable<MolgenisUser> superUsers = dataService.findAll(MolgenisUser.ENTITY_NAME,
				new QueryImpl().eq(MolgenisUser.SUPERUSER, true), MolgenisUser.class);

		return superUsers != null ? Lists.transform(Lists.newArrayList(superUsers),
				new Function<MolgenisUser, String>()
				{
					@Override
					public String apply(MolgenisUser molgenisUser)
					{
						return molgenisUser.getEmail();
					}
				}) : Collections.<String> emptyList();
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
		Iterable<MolgenisGroupMember> molgenisGroupMembers = dataService.findAll(MolgenisGroupMember.ENTITY_NAME,
				new QueryImpl().eq(MolgenisGroupMember.MOLGENISUSER, getUser(username)), MolgenisGroupMember.class);
		return Iterables.transform(molgenisGroupMembers, new Function<MolgenisGroupMember, MolgenisGroup>()
		{

			@Override
			public MolgenisGroup apply(MolgenisGroupMember molgenisGroupMember)
			{
				return molgenisGroupMember.getMolgenisGroup();
			}
		});
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
