package org.molgenis.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RoleFactory extends AbstractSystemEntityFactory<Role, RoleMetadata, String>
{
	@Autowired
	RoleFactory(RoleMetadata roleMetadata, EntityPopulator entityPopulator)
	{
		super(Role.class, roleMetadata, entityPopulator);
	}
}
