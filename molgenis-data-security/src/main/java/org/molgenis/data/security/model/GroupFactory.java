package org.molgenis.data.security.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class GroupFactory extends AbstractSystemEntityFactory<GroupEntity, GroupMetadata, String>
{
	GroupFactory(GroupMetadata groupMetaData, EntityPopulator entityPopulator)
	{
		super(GroupEntity.class, groupMetaData, entityPopulator);
	}
}
