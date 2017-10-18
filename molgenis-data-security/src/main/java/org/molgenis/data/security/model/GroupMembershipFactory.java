package org.molgenis.data.security.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class GroupMembershipFactory
		extends AbstractSystemEntityFactory<GroupMembershipEntity, GroupMembershipMetadata, String>
{
	GroupMembershipFactory(GroupMembershipMetadata groupMemberMetaData, EntityPopulator entityPopulator)
	{
		super(GroupMembershipEntity.class, groupMemberMetaData, entityPopulator);
	}
}
