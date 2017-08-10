package org.molgenis.security.twofactor.auth;

import static org.apache.commons.lang3.StringUtils.capitalize;

/**
 * <p>You can have tw-factor-authentication in three different states in your system</p>
 * <ul>
 * <li>DISABLED: two-factor-authentication is disabled for all users</li>
 * <li>ENABLED: two-factor-authentication is enabled. Users CAN determine for themselves if they want to configure it in MOLGENIS</li>
 * <li>ENFORCES: two-factor-authentication is enforced for all users. Users HAVE TO configure two-factor-authentication for their account.</li>
 * </ul>
 */
public enum TwoFactorAuthenticationSetting
{
	ENABLED, DISABLED, ENFORCED;

	@Override
	public String toString()
	{
		return capitalize(name().toLowerCase());
	}
}
