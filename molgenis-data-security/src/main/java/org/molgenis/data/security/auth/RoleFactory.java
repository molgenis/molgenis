package org.molgenis.data.security.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class RoleFactory extends AbstractSystemEntityFactory<Role, RoleMetadata, String>
{
	RoleFactory(RoleMetadata roleMetadata, EntityPopulator entityPopulator)
	{
		super(Role.class, roleMetadata, entityPopulator);
	}
}
