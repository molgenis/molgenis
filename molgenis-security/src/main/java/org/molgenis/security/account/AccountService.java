package org.molgenis.security.account;

import org.molgenis.auth.MolgenisUser;

public interface AccountService
{
	public static final String ALL_USER_GROUP = "All Users";

	void createUser(MolgenisUser molgenisUser, String baseActivationUri);

	/**
	 * Activate a registered user
	 * 
	 * @param activationCode
	 */
	void activateUser(String activationCode);

	void changePassword(String username, String newPassword);

	void resetPassword(String userEmail);
}