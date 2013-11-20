package org.molgenis.security.user;

import org.molgenis.framework.db.DatabaseException;
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
	 * @throws DatabaseException
	 */
	MolgenisUser getUser(String username);

	/**
	 * Update the currently logged in user
	 * 
	 * @param molgenisUser
	 *            updated user
	 * @throws DatabaseException
	 */
	void updateCurrentUser(MolgenisUser molgenisUser, String username);

	/**
	 * Validates the password of the current logged in user
	 * 
	 * @param password
	 * @return
	 * @throws DatabaseException
	 */
	boolean validateCurrentUserPassword(String password, String username);
}
