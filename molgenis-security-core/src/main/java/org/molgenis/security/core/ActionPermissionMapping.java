package org.molgenis.security.core;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ActionPermissionMapping
{
	public abstract Action getAction();

	public abstract Permission getPermission();
}
