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
	private final Database unsecuredDatabase;
	private final PasswordEncoder passwordEncoder;

	@Autowired
	public MolgenisUserServiceImpl(Database database, Database unsecuredDatabase, PasswordEncoder passwordEncoder)
	{
		if (database == null) throw new IllegalArgumentException("Database is null");
		if (unsecuredDatabase == null) throw new IllegalArgumentException("Database is null");
		if (passwordEncoder == null) throw new IllegalArgumentException("MolgenisPasswordEncoder is null");
		this.database = database;
		this.unsecuredDatabase = unsecuredDatabase;
		this.passwordEncoder = passwordEncoder;
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
	public MolgenisUser findById(Integer userId) throws DatabaseException
	{
		return MolgenisUser.findById(database, userId);
	}

	@Override
	public void update(MolgenisUser updatedUser) throws DatabaseException
	{
		MolgenisUser currentUser = MolgenisUser.findByUsername(database, updatedUser.getUsername());
		if (currentUser == null)
		{
			throw new RuntimeException("User does not exist [" + updatedUser.getUsername() + "]");
		}
		String password = currentUser.getPassword();
		String newPassword = updatedUser.getPassword();
		if (StringUtils.isNotEmpty(newPassword) && !password.equals(newPassword))
		{
			if (!passwordEncoder.matches(newPassword, currentUser.getPassword()))
			{
				throw new MolgenisUserException("Wrong password");
			}
			String encryptedPassword = passwordEncoder.encode(newPassword);
			updatedUser.setPassword(encryptedPassword);
		}
		unsecuredDatabase.update(updatedUser);
	}

	@Override
	public MolgenisUser getCurrentUser() throws DatabaseException
	{
		String currentUsername = SecurityUtils.getCurrentUsername();
		return MolgenisUser.findByUsername(database, currentUsername);
	}
}
