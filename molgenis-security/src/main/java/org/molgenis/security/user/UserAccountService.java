package org.molgenis.security.user;

import org.molgenis.omx.auth.MolgenisUser;

/**
 * Manage account of the current user
 */
public interface UserAccountService
{
	/**
	 * Returns the currently logged in user
	 * 
	 * @return
	 */
	MolgenisUser getCurrentUser();

	/**
	 * Update the currently logged in user
	 * 
	 * @param molgenisUser
	 *            updated user
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
