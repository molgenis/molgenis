package org.molgenis.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GroupFactory extends AbstractSystemEntityFactory<Group, GroupMetaData, String>
{
	@Autowired
	GroupFactory(GroupMetaData groupMetaData, EntityPopulator entityPopulator)
	{
		super(Group.class, groupMetaData, entityPopulator);
	}
}
