package org.molgenis.data.security.acl;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EntityIdentity.class)
public abstract class EntityIdentity
{
	public abstract String getEntityTypeId();

	public abstract Object getEntityId();

	public static EntityIdentity create(String newEntityTypeId, Object newEntityId)
	{
		return new AutoValue_EntityIdentity(newEntityTypeId, newEntityId);
	}
}
