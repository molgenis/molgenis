package org.molgenis.security.user;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

@Service
public class MolgenisUserServiceImpl implements MolgenisUserService
{
	private final Database database;
	private final PasswordEncoder passwordEncoder;

	@Autowired
	public MolgenisUserServiceImpl(Database database, PasswordEncoder passwordEncoder)
	{
		if (database == null) throw new IllegalArgumentException("Database is null");
		if (passwordEncoder == null) throw new IllegalArgumentException("MolgenisPasswordEncoder is null");
		this.database = database;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public List<String> getSuEmailAddresses() throws DatabaseException
	{
		List<MolgenisUser> superUsers = database.find(MolgenisUser.class, new QueryRule(MolgenisUser.SUPERUSER,
				Operator.EQUALS, true));
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
	public MolgenisUser findById(Integer userId) throws DatabaseException
	{
		return MolgenisUser.findById(database, userId);
	}

	@Override
	public void update(MolgenisUser molgenisUser) throws DatabaseException
	{
		database.update(molgenisUser);
	}

	@Override
	public MolgenisUser getCurrentUser() throws DatabaseException
	{
		String currentUsername = SecurityUtils.getCurrentUsername();
		return MolgenisUser.findByUsername(database, currentUsername);
	}

	@Override
	public void checkPassword(String userName, String oldPwd, String newPwd1, String newPwd2) throws DatabaseException
	{
		if (StringUtils.isEmpty(oldPwd) || StringUtils.isEmpty(newPwd1) || StringUtils.isEmpty(newPwd2))
		{
			throw new MolgenisUserException("Passwords empty");
		}
		if (!StringUtils.equals(newPwd1, newPwd2)) throw new MolgenisUserException("Passwords do not match");

		MolgenisUser user = MolgenisUser.findByUsername(database, userName);
		if (user == null)
		{
			throw new RuntimeException("User does not exist [" + userName + "]");
		}
		if (!passwordEncoder.matches(oldPwd, user.getPassword()))
		{
			throw new MolgenisUserException("Wrong password");
		}
	}
}
