package org.molgenis.data.security.acl;

import com.google.auto.value.AutoValue;
import org.molgenis.security.core.Permission;

@AutoValue
public abstract class EntityAce
{
	public abstract Permission getPermission();

	public abstract SecurityId getSecurityId();

	public abstract boolean isGranting();

	public static EntityAce create(Permission permission, SecurityId securityId, boolean granting)
	{
		return new AutoValue_EntityAce(permission, securityId, granting);
	}
}
