package org.molgenis.data.security.acl;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class EntityIdentity
{
	public abstract String getEntityTypeId();

	public abstract Object getEntityId();

	public static EntityIdentity create(String newEntityTypeId, Object newEntityId)
	{
		return new AutoValue_EntityIdentity(newEntityTypeId, newEntityId);
	}
}
