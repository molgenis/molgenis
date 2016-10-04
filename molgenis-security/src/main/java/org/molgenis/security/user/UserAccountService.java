package org.molgenis.security.user;

import org.molgenis.auth.MolgenisGroup;
import org.molgenis.auth.MolgenisUser;

/**
 * Manage account of the current user
 */
public interface UserAccountService
{
	int MIN_PASSWORD_LENGTH = 6;

	/**
	 * Returns the currently logged in user
	 *
	 * @return
	 */
	MolgenisUser getCurrentUser();

	/**
	 * Returns the groups to which the currently logged in user belongs
	 *
	 * @return
	 */
	Iterable<MolgenisGroup> getCurrentUserGroups();

	/**
	 * Update the currently logged in user
	 *
	 * @param molgenisUser updated user
	 */
	void updateCurrentUser(MolgenisUser molgenisUser);

	/**
	 * Validates the password of the current logged in user
	 *
	 * @param password
	 * @return
	 */
	boolean validateCurrentUserPassword(String password);
}
