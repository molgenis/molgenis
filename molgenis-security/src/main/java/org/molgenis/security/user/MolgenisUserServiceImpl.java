package org.molgenis.security.user;

import java.util.Collections;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.SecurityUtils;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
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
		List<MolgenisUser> superUsers = dataService.findAllAsList(MolgenisUser.ACTIVATIONCODE,
				new QueryImpl().eq(MolgenisUser.SUPERUSER, true));

		return superUsers != null ? Lists.transform(superUsers, new Function<MolgenisUser, String>()
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
	public MolgenisUser getCurrentUser()
	{
		String currentUsername = SecurityUtils.getCurrentUsername();
		return dataService
				.findOne(MolgenisUser.ENTITY_NAME, new QueryImpl().eq(MolgenisUser.USERNAME, currentUsername));
	}
}
