package org.molgenis.security.user;

import org.molgenis.auth.Group;
import org.molgenis.auth.User;

import java.util.List;

public interface UserService
{
	/**
	 * Returns e-mail addresses of super users
	 *
	 * @return
	 */
	List<String> getSuEmailAddresses();

	/**
	 * Returns the given user
	 */
	User getUser(String username);

	/**
	 * Find a user by it's email.
	 *
	 * @param email
	 * @return the user or null if not found
	 */
	User getUserByEmail(String email);

	/**
	 * Returns the groups that the given user belongs to
	 *
	 * @param username
	 * @return
	 */
	Iterable<Group> getUserGroups(String username);

	/**
	 * Update user
	 *
	 * @param user
	 */
	void update(User user);
}
