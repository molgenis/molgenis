package org.molgenis.data.security;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.security.acls.domain.ObjectIdentityImpl;

public class EntityIdentity extends ObjectIdentityImpl
{
	public EntityIdentity(EntityType entityType, Entity entity)
	{
		this(entityType.getId(), entity.getIdValue());
	}

	public EntityIdentity(String entityTypeId, Object entityId)
	{
		super(EntityIdentityUtils.toType(entityTypeId), entityId.toString());
	}
}
