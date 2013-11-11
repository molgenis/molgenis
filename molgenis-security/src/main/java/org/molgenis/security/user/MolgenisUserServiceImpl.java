package org.molgenis.security.user;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.SecurityUtils;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

@Service
public class MolgenisUserServiceImpl implements MolgenisUserService
{
	private final DataService dataService;
	private final PasswordEncoder passwordEncoder;

	@Autowired
	public MolgenisUserServiceImpl(DataService dataService, PasswordEncoder passwordEncoder)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		if (passwordEncoder == null) throw new IllegalArgumentException("MolgenisPasswordEncoder is null");
		this.dataService = dataService;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@RunAsSystem
	public List<String> getSuEmailAddresses()
	{
		List<MolgenisUser> superUsers = dataService.findAllAsList(MolgenisUser.ENTITY_NAME, new QueryRule(
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
	public MolgenisUser findById(Integer userId)
	{
		return dataService.findOne(MolgenisUser.ENTITY_NAME, userId);
	}

	@Override
	public void update(MolgenisUser molgenisUser)
	{
		MolgenisUser currentMolgenisUser = findById(molgenisUser.getId());
		if (!currentMolgenisUser.getPassword().equals(molgenisUser.getPassword()))
		{
			String encryptedPassword = passwordEncoder.encode(molgenisUser.getPassword());
			molgenisUser.setPassword(encryptedPassword);
		}

		dataService.update(MolgenisUser.ENTITY_NAME, molgenisUser);
	}

	@Override
	public MolgenisUser getCurrentUser()
	{
		String currentUsername = SecurityUtils.getCurrentUsername();
		return dataService
				.findOne(MolgenisUser.ENTITY_NAME, new QueryImpl().eq(MolgenisUser.USERNAME, currentUsername));
	}

	@Override
	public void checkPassword(String userName, String oldPwd, String newPwd1, String newPwd2)
	{
		if (StringUtils.isEmpty(oldPwd) || StringUtils.isEmpty(newPwd1) || StringUtils.isEmpty(newPwd2))
		{
			throw new MolgenisUserException("Passwords empty");
		}
		if (!StringUtils.equals(newPwd1, newPwd2)) throw new MolgenisUserException("Passwords do not match");

		MolgenisUser user = dataService.findOne(MolgenisUser.ENTITY_NAME,
				new QueryImpl().eq(MolgenisUser.USERNAME, userName));

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
