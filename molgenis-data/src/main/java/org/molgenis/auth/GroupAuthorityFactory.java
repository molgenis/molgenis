package org.molgenis.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GroupAuthorityFactory extends AbstractSystemEntityFactory<GroupAuthority, GroupAuthorityMetaData, String>
{
	@Autowired
	GroupAuthorityFactory(GroupAuthorityMetaData groupAuthorityMetaData)
	{
		super(GroupAuthority.class, groupAuthorityMetaData, String.class);
	}
}
