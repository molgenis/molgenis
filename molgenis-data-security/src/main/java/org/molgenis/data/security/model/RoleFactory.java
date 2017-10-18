package org.molgenis.data.security.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class RoleFactory extends AbstractSystemEntityFactory<RoleEntity, RoleMetadata, String>
{
	RoleFactory(RoleMetadata roleMetadata, EntityPopulator entityPopulator)
	{
		super(RoleEntity.class, roleMetadata, entityPopulator);
	}
}
