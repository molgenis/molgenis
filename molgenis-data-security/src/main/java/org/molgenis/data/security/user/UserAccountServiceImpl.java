package org.molgenis.data.security.user;

import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.UserAccountService;
import org.molgenis.security.core.service.UserService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class UserAccountServiceImpl implements UserAccountService
{
	private final UserService userService;
	private final PasswordEncoder passwordEncoder;

	public UserAccountServiceImpl(UserService userService, PasswordEncoder passwordEncoder)
	{
		this.userService = Objects.requireNonNull(userService);
		this.passwordEncoder = Objects.requireNonNull(passwordEncoder);
	}

	@Override
	public void updateCurrentUser(User updatedCurrentUser)
	{
		// TODO: Use Row level security for this
		String currentUsername = SecurityUtils.getCurrentUsername()
											  .orElseThrow(() -> new IllegalStateException("Current user not found."));
		if (!currentUsername.equals(updatedCurrentUser.getUsername()))
		{
			throw new IllegalArgumentException("Can only update current user.");
		}
		userService.update(updatedCurrentUser);
	}

	@Override
	public boolean validateCurrentUserPassword(String password)
	{
		return password != null && !password.isEmpty() && passwordEncoder.matches(password,
				getCurrentUser().getPassword());
	}

	@Override
	public Optional<User> getCurrentUserIfPresent()
	{
		return SecurityUtils.getCurrentUsername().flatMap(userService::findByUsernameIfPresent);
	}
}
