package org.molgenis.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GroupMemberFactory extends AbstractSystemEntityFactory<GroupMember, GroupMemberMetaData, String>
{
	@Autowired
	GroupMemberFactory(GroupMemberMetaData groupMemberMetaData, EntityPopulator entityPopulator)
	{
		super(GroupMember.class, groupMemberMetaData, entityPopulator);
	}
}
