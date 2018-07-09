package org.molgenis.security.group;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AddGroupMemberCommand.class)
@SuppressWarnings("squid:S1610")
public abstract class AddGroupMemberCommand
{
	static AddGroupMemberCommand create(String username, String roleName) {
		return new AutoValue_AddGroupMemberCommand(username, roleName);
	}

	public abstract String getUsername();
	public abstract String getRoleName();
}
