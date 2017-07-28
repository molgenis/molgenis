package org.molgenis.security.user;

import org.molgenis.auth.Group;
import org.molgenis.auth.User;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountServiceImpl implements UserAccountService
{
	@Autowired
	private UserService userService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	@Transactional(readOnly = true)
	public User getCurrentUser()
	{
		return userService.getUser(SecurityUtils.getCurrentUsername());
	}

	@Override
	@Transactional(readOnly = true)
	public Iterable<Group> getCurrentUserGroups()
	{
		return userService.getUserGroups(SecurityUtils.getCurrentUsername());
	}

	@Override
	@Transactional
	public void updateCurrentUser(User updatedCurrentUser)
	{
		String currentUsername = SecurityUtils.getCurrentUsername();
		if (!currentUsername.equals(updatedCurrentUser.getUsername()))
		{
			throw new RuntimeException("Updated user differs from the current user");
		}

		User currentUser = userService.getUser(currentUsername);
		if (currentUser == null)
		{
			throw new RuntimeException("User does not exist [" + currentUsername + "]");
		}
		userService.update(updatedCurrentUser);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional
	public boolean validateCurrentUserPassword(String password)
	{
		if (password == null || password.isEmpty()) return false;

		String currentUsername = SecurityUtils.getCurrentUsername();
		User currentUser = userService.getUser(currentUsername);
		if (currentUser == null)
		{
			throw new RuntimeException("User does not exist [" + SecurityUtils.getCurrentUsername() + "]");
		}
		return passwordEncoder.matches(password, currentUser.getPassword());
	}
}
