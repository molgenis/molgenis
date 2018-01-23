package org.molgenis.data.security.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class GroupAuthorityFactory extends AbstractSystemEntityFactory<GroupAuthority, GroupAuthorityMetaData, String>
{
	GroupAuthorityFactory(GroupAuthorityMetaData groupAuthorityMetaData, EntityPopulator entityPopulator)
	{
		super(GroupAuthority.class, groupAuthorityMetaData, entityPopulator);
	}
}
