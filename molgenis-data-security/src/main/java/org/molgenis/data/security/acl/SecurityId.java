package org.molgenis.data.security.acl;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class SecurityId
{
	@Nullable
	public abstract String getUsername();

	@Nullable
	public abstract String getAuthority();

	public static SecurityId create(String newUsername, String newAuthority)
	{
		if ((newUsername == null && newAuthority == null) || (newUsername != null && newAuthority != null))
		{
			throw new IllegalArgumentException("Either username or authority must be non-null");
		}
		return new AutoValue_SecurityId(newUsername, newAuthority);
	}
}
