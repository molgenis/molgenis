package org.molgenis.data.security;

import org.molgenis.data.Entity;
import org.springframework.security.acls.domain.ObjectIdentityImpl;

import java.io.Serializable;

public class EntityIdentity extends ObjectIdentityImpl
{
	public EntityIdentity(Entity entity)
	{
		this(entity.getEntityType().getId(), entity.getIdValue());
	}

	public EntityIdentity(String entityTypeId, Object entityId)
	{
		super(EntityIdentityUtils.toType(entityTypeId), (Serializable) entityId);
	}
}
