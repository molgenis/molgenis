package org.molgenis.security.group;

import com.google.auto.value.AutoValue;
import org.molgenis.data.security.auth.Role;

@AutoValue
public abstract class RoleResponse
{
	public abstract String getRoleName();
	public abstract String getRoleLabel();

	static RoleResponse fromEntity(Role role) {
		return new AutoValue_RoleResponse(role.getName(), role.getLabel());
	}
}
