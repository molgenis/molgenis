package org.molgenis.security.twofactor.auth;

import static org.apache.commons.lang3.StringUtils.capitalize;

public enum TwoFactorAuthenticationSetting
{
	ENABLED, DISABLED, ENFORCED;

	@Override
	public String toString()
	{
		return capitalize(name().toLowerCase());
	}
}
