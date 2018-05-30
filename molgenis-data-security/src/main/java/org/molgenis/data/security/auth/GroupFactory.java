package org.molgenis.data.security.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class GroupFactory extends AbstractSystemEntityFactory<Group, GroupMetadata, String>
{
	GroupFactory(GroupMetadata groupMetadata, EntityPopulator entityPopulator)
	{
		super(Group.class, groupMetadata, entityPopulator);
	}
}
