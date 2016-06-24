package org.molgenis.security.account;

public enum ActivationMode
{
	ADMIN, USER;

	public static ActivationMode from(String str, ActivationMode defaultActivationMode)
	{
		if (str == null) return defaultActivationMode;
		for (ActivationMode activationMode : ActivationMode.values())
			if (str.equalsIgnoreCase(activationMode.toString())) return activationMode;
		return defaultActivationMode;
	}
}