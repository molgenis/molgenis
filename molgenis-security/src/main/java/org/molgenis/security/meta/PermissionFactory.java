package org.molgenis.security.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class PermissionFactory extends AbstractSystemEntityFactory<Permission, PermissionMetadata, String>
{
	PermissionFactory(PermissionMetadata permissionMetadata, EntityPopulator entityPopulator)
	{
		super(Permission.class, permissionMetadata, entityPopulator);
	}
}
