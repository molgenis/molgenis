package org.molgenis.security.user;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.SecurityUtils;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountServiceImpl implements UserAccountService
{
	@Autowired
	private DataService dataService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU', 'ROLE_PLUGIN_READ_USERACCOUNT')")
	@Transactional(readOnly = true)
	@RunAsSystem
	public MolgenisUser getUser(String userName)
	{
		return dataService.findOne(MolgenisUser.ENTITY_NAME, new QueryImpl().eq(MolgenisUser.USERNAME, userName));
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU', 'ROLE_PLUGIN_WRITE_USERACCOUNT')")
	@Transactional
	@RunAsSystem
	public void updateCurrentUser(MolgenisUser updatedCurrentUser, String currentUsername)
	{
		if (!currentUsername.equals(updatedCurrentUser.getUsername()))
		{
			throw new RuntimeException("Updated user differs from the current user");
		}

		MolgenisUser currentUser = getUser(currentUsername);
		if (currentUser == null)
		{
			throw new RuntimeException("User does not exist [" + currentUsername + "]");
		}
		String password = currentUser.getPassword();
		String updatedPassword = updatedCurrentUser.getPassword();
		if (StringUtils.isNotEmpty(updatedPassword) && !password.equals(updatedPassword))
		{
			// encode updated password
			String encodedPassword = passwordEncoder.encode(updatedPassword);
			updatedCurrentUser.setPassword(encodedPassword);
		}

		dataService.update(MolgenisUser.ENTITY_NAME, updatedCurrentUser);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU', 'ROLE_PLUGIN_READ_USERACCOUNT')")
	@Transactional
	@RunAsSystem
	public boolean validateCurrentUserPassword(String password, String username)
	{
		if (password == null || password.isEmpty()) return false;

		MolgenisUser currentUser = getUser(username);
		if (currentUser == null)
		{
			throw new RuntimeException("User does not exist [" + SecurityUtils.getCurrentUsername() + "]");
		}
		return passwordEncoder.matches(password, currentUser.getPassword());
	}
}
