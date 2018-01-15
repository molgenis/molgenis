package org.molgenis.data.security.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class GroupMemberFactory extends AbstractSystemEntityFactory<GroupMember, GroupMemberMetaData, String>
{
	GroupMemberFactory(GroupMemberMetaData groupMemberMetaData, EntityPopulator entityPopulator)
	{
		super(GroupMember.class, groupMemberMetaData, entityPopulator);
	}
}
