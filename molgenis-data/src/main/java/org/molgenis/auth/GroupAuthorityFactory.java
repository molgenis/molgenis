package org.molgenis.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GroupAuthorityFactory extends AbstractSystemEntityFactory<GroupAuthority, GroupAuthorityMetaData, String>
{
	@Autowired
	GroupAuthorityFactory(GroupAuthorityMetaData groupAuthorityMetaData, EntityPopulator entityPopulator)
	{
		super(GroupAuthority.class, groupAuthorityMetaData, entityPopulator);
	}
}
