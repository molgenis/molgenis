package org.molgenis.security.user;

import java.util.Collections;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

@Service
public class MolgenisUserServiceImpl implements MolgenisUserService
{
	private final Database unsecuredDatabase;

	@Autowired
	public MolgenisUserServiceImpl(Database unsecuredDatabase)
	{
		if (unsecuredDatabase == null) throw new IllegalArgumentException("Database is null");
		this.unsecuredDatabase = unsecuredDatabase;
	}

	@Override
	public List<String> getSuEmailAddresses() throws DatabaseException
	{
		List<MolgenisUser> superUsers = unsecuredDatabase.find(MolgenisUser.class, new QueryRule(
				MolgenisUser.SUPERUSER, Operator.EQUALS, true));
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
	public MolgenisUser getCurrentUser() throws DatabaseException
	{
		String currentUsername = SecurityUtils.getCurrentUsername();
		return MolgenisUser.findByUsername(unsecuredDatabase, currentUsername);
	}
}
