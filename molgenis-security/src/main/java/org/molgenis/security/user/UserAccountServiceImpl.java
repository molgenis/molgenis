package org.molgenis.security.user;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountServiceImpl implements UserAccountService
{
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private MolgenisUserService userService;

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU', 'ROLE_PLUGIN_READ_USERACCOUNT')")
	@Transactional(readOnly = true)
	public MolgenisUser getCurrentUser()
	{
		return userService.getUser(SecurityUtils.getCurrentUsername());
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU', 'ROLE_PLUGIN_WRITE_USERACCOUNT')")
	@Transactional
	public void updateCurrentUser(MolgenisUser updatedCurrentUser)
	{
		String currentUsername = SecurityUtils.getCurrentUsername();
		if (!currentUsername.equals(updatedCurrentUser.getUsername()))
		{
			throw new RuntimeException("Updated user differs from the current user");
		}

		MolgenisUser currentUser = userService.getUser(currentUsername);
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

		userService.update(updatedCurrentUser);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU', 'ROLE_PLUGIN_READ_USERACCOUNT')")
	@Transactional
	public boolean validateCurrentUserPassword(String password)
	{
		if (password == null || password.isEmpty()) return false;

		String currentUsername = SecurityUtils.getCurrentUsername();
		MolgenisUser currentUser = userService.getUser(currentUsername);
		if (currentUser == null)
		{
			throw new RuntimeException("User does not exist [" + SecurityUtils.getCurrentUsername() + "]");
		}
		return passwordEncoder.matches(password, currentUser.getPassword());
	}
}
