package org.molgenis.security.twofactor.service;

import org.molgenis.security.twofactor.model.RecoveryCode;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.stream.Stream;

/**
 * Service that manages a user's recovery codes. These codes can be used to unlock an account when two factor
 * authentication is enabled and the user loses his/her phone.
 */
public interface RecoveryService
{
	/**
	 * Generates new recovery codes for the current user and deletes the old codes (if any).
	 *
	 * @return stream of the new RecoveryCodes
	 */
	Stream<RecoveryCode> generateRecoveryCodes();

	/**
	 * Checks if a recovery code is valid for the current user and throws an exception if it is not. The code is removed
	 * after a successful recovery attempt.
	 *
	 * @param recoveryCode the code to validate
	 * @throws BadCredentialsException if the recovery code is incorrect
	 */
	void useRecoveryCode(String recoveryCode);

	/**
	 * Get the recovery codes (if any) of the current user.
	 *
	 * @return stream of recovery codes
	 */
	Stream<RecoveryCode> getRecoveryCodes();
}
