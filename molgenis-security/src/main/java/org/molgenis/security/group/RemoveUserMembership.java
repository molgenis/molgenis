package org.molgenis.security.group;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_RemoveUserMembership.class)
@SuppressWarnings("squid:S1610")
public abstract class RemoveUserMembership
{
	public abstract String getUserId();

	public abstract String getGroupId();
}
