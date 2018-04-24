package org.molgenis.security.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class ActionPermissionMappingFactory
		extends AbstractSystemEntityFactory<ActionPermissionMapping, ActionPermissionMappingMetadata, String>
{
	ActionPermissionMappingFactory(ActionPermissionMappingMetadata actionPermissionMappingMetadata,
			EntityPopulator entityPopulator)
	{
		super(ActionPermissionMapping.class, actionPermissionMappingMetadata, entityPopulator);
	}
}
