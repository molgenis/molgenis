package org.molgenis.security.twofactor;

public enum TwoFactorAuthenticationSetting
{
	ENABLED, DISABLED, ENFORCED;

	@Override
	public String toString()
	{
		return name().toLowerCase();
	}
}
