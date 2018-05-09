package org.molgenis.data.security.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class GroupMembershipFactory
		extends AbstractSystemEntityFactory<GroupMembership, GroupMembershipMetadata, String>
{
	GroupMembershipFactory(GroupMembershipMetadata groupMembershipMetadata, EntityPopulator entityPopulator)
	{
		super(GroupMembership.class, groupMembershipMetadata, entityPopulator);
	}
}
