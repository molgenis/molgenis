package org.molgenis.data.security.acl;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.EntityUtils;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Maps domain object instance identity to/from entity identifier.
 */
@Component
class ObjectIdMapper
{
	private final DataService dataService;

	ObjectIdMapper(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	ObjectIdentity toObjectIdentity(Entity entity)
	{
		return toObjectIdentity(entity.getEntityType().getId(), entity.getIdValue());
	}

	ObjectIdentity toObjectIdentity(String entityTypeId, Object entityId)
	{
		return new ObjectIdentityImpl(entityTypeId, entityId.toString());
	}

	String toEntityTypeId(ObjectIdentity objectIdentity)
	{
		return objectIdentity.getType();
	}

	Object toEntityId(ObjectIdentity objectIdentity)
	{
		String entityTypeId = objectIdentity.getType();
		EntityType entityType = dataService.getEntityType(entityTypeId);
		String untypedIdentifier = objectIdentity.getIdentifier().toString();
		return EntityUtils.getTypedValue(untypedIdentifier, entityType.getIdAttribute());
	}
}
