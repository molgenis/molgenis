package org.molgenis.security.account;

import org.molgenis.auth.MolgenisUser;

public interface AccountService
{
	public static final String ALL_USER_GROUP = "All Users";
	public static final String KEY_PLUGIN_AUTH_ACTIVATIONMODE = "plugin.auth.activation_mode";
	public static final String KEY_PLUGIN_AUTH_ENABLE_SELFREGISTRATION = "plugin.auth.enable_self_registration";

	void createUser(MolgenisUser molgenisUser, String baseActivationUri);

	/**
	 * Activate a registered user
	 * 
	 * @param activationCode
	 */
	void activateUser(String activationCode);

	void changePassword(String username, String newPassword);

	void resetPassword(String userEmail);

	ActivationMode getActivationMode();

	boolean isSelfRegistrationEnabled();
}