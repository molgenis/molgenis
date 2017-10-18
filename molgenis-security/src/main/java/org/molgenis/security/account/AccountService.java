package org.molgenis.security.account;

import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.EmailAlreadyExistsException;
import org.molgenis.security.core.service.UsernameAlreadyExistsException;

/**
 * User account management.
 */
public interface AccountService
{
	/**
	 * Registers a new {@link org.molgenis.security.core.model.User} and sends activation email to the relevant addresses.
	 *
	 * @param registerRequest   {@link RegisterRequest} for the new user
	 * @param baseActivationUri URI where the user can be activated
	 * @return the registered User
	 * @throws UsernameAlreadyExistsException if the username is already taken
	 * @throws EmailAlreadyExistsException    if the email address is already taken
	 */
	User register(RegisterRequest registerRequest, String baseActivationUri)
			throws UsernameAlreadyExistsException, EmailAlreadyExistsException;

	/**
	 * Activates an inactive user
	 */
	void activateUser(String activationCode);

	/**
	 * Changes a {@link User}'s password.
	 * @param username username of the user
	 * @param newPassword new password
	 */
	void changePassword(String username, String newPassword);

	/**
	 * Resets a user's password and sends the new password to their email address.
	 *
	 * @param email email address of the user
	 */
	void resetPassword(String email);
}