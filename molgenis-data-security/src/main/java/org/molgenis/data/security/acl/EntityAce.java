package org.molgenis.data.security.acl;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;
import org.molgenis.security.core.Permission;

import java.util.Set;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EntityAce.class)
public abstract class EntityAce
{
	public abstract Set<Permission> getPermissions();

	public abstract SecurityId getSecurityId();

	public abstract boolean isGranting();

	public static EntityAce create(Set<Permission> permissions, SecurityId securityId, boolean granting)
	{
		return new AutoValue_EntityAce(permissions, securityId, granting);
	}
}
