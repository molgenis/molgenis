package org.molgenis.security.core.service;

import org.molgenis.security.core.model.User;

import java.util.Optional;

/**
 * Manages the currently logged in {@link User}.
 */
public interface UserAccountService
{
	int MIN_PASSWORD_LENGTH = 6;

	/**
	 * Retrieves the currently logged in user.
	 *
	 * @throws IllegalStateException if no user is currently logged in
	 * @apiNote This does not mean that the user is also authenticated
	 */
	default User getCurrentUser()
	{
		return getCurrentUserIfPresent().orElseThrow(() -> new IllegalStateException("Current user not found"));
	}

	/**
	 * Retrieves the currently logged in user if present.
	 *
	 * @return Optional User
	 */
	Optional<User> getCurrentUserIfPresent();

	/**
	 * Update the currently logged in user
	 *
	 * @param user updated user
	 */
	void updateCurrentUser(User user);

	/**
	 * Validates the password of the currently logged in user
	 */
	boolean validateCurrentUserPassword(String password);
}
