package org.molgenis.data.security;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.security.acls.domain.ObjectIdentityImpl;

public class RepositoryIdentity extends ObjectIdentityImpl
{
	public static final String TYPE = "repository";

	public RepositoryIdentity(Repository<Entity> repository)
	{
		this(repository.getEntityType());
	}

	public RepositoryIdentity(EntityType entityType)
	{
		this(entityType.getId());
	}

	public RepositoryIdentity(String entityTypeId)
	{
		super(TYPE, entityTypeId);
	}
}
