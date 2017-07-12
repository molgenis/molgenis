package org.molgenis.data.security.acl;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SecurityId
{
	public enum Type
	{
		USER, GROUP
	}

	public abstract String getId();

	public abstract Type getType();

	public static SecurityId create(String newId, Type newType)
	{
		return new AutoValue_SecurityId(newId, newType);
	}
}
