package org.molgenis.security.account;

import org.molgenis.auth.User;

public interface AccountService
{
	String ALL_USER_GROUP = "All Users";

	void createUser(User user, String baseActivationUri)
			throws UsernameAlreadyExistsException, EmailAlreadyExistsException;

	/**
	 * Activate a registered user
	 *
	 * @param activationCode
	 */
	void activateUser(String activationCode);

	void changePassword(String username, String newPassword);

	void resetPassword(String userEmail);
}