package org.molgenis.data.security.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class GroupFactory extends AbstractSystemEntityFactory<Group, GroupMetaData, String>
{
	GroupFactory(GroupMetaData groupMetaData, EntityPopulator entityPopulator)
	{
		super(Group.class, groupMetaData, entityPopulator);
	}
}
