package org.molgenis.data.security.acl;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SecurityId
{
	public abstract String getId();

	public static SecurityId create(String newId)
	{
		return new AutoValue_SecurityId(newId);
	}
}
