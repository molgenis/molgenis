package org.molgenis.data.security;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.security.acls.domain.ObjectIdentityImpl;

public class EntityIdentity extends ObjectIdentityImpl
{
	private static final String TYPE_PREFIX = "entity";

	public EntityIdentity(EntityType entityType, Entity entity)
	{
		this(entityType.getId(), entity.getIdValue());
	}

	public EntityIdentity(String entityTypeId, Object entityId)
	{
		super(TYPE_PREFIX + '-' + entityTypeId, entityId.toString());
	}
}
