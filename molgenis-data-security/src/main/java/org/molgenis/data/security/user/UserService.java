package org.molgenis.data.security.user;

import org.molgenis.data.security.auth.User;

import java.util.List;

public interface UserService
{
	/**
	 * Returns e-mail addresses of super users
	 */
	List<String> getSuEmailAddresses();

	/**
	 * Returns the given user
	 */
	User getUser(String username);

	/**
	 * Find a user by it's email.
	 *
	 * @return the user or null if not found
	 */
	User getUserByEmail(String email);

	/**
	 * Update user
	 */
	void update(User user);
}
