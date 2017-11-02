package org.molgenis.security.group;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_GroupRole.class)
@SuppressWarnings("squid:S1610")
public abstract class GroupRole
{

	public abstract String getGroupId();

	public abstract String getRoleId();

}
