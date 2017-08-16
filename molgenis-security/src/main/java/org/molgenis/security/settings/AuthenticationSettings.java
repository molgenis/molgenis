package org.molgenis.security.settings;

import org.molgenis.security.twofactor.auth.TwoFactorAuthenticationSetting;

public interface AuthenticationSettings
{
	/**
	 * @return whether sign up is enabled
	 */
	boolean getSignUp();

	/**
	 * @param signUp <code>true</code> if sign up is enabled
	 */
	void setSignUp(boolean signUp);

	/**
	 * @return whether sign up is moderated
	 */
	boolean getSignUpModeration();

	/**
	 * @param signUpModeration <code>true</code> if sign up is moderated
	 */
	void setSignUpModeration(boolean signUpModeration);

	/**
	 * @param signIn whether sign in is enabled
	 */
	void setGoogleSignIn(boolean signIn);

	/**
	 * @return <code>true</code> if sign in is enabled
	 */
	boolean getGoogleSignIn();

	/**
	 * @param googleAppClientId Google app client ID used during Google Sign-In
	 */
	void setGoogleAppClientId(String googleAppClientId);

	/**
	 * @return Google app client ID used during Google Sign-In
	 */
	String getGoogleAppClientId();

	/**
	 * @param twoFactorAuthentication whether two factor authentication is disabled, enabled or enforced
	 */
	void setTwoFactorAuthentication(TwoFactorAuthenticationSetting twoFactorAuthentication);

	/**
	 * @return <code>enabled</code> or <code>enforced</code> if two factor authentication is optional or mandatory, <code>disabled</code> when it is off
	 */
	TwoFactorAuthenticationSetting getTwoFactorAuthentication();
}
