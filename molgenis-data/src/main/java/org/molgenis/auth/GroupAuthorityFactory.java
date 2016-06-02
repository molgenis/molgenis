package org.molgenis.auth;

import org.molgenis.data.AbstractEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GroupAuthorityFactory extends AbstractEntityFactory<GroupAuthority, GroupAuthorityMetaData, String>
{
	@Autowired
	GroupAuthorityFactory(GroupAuthorityMetaData groupAuthorityMetaData)
	{
		super(GroupAuthority.class, groupAuthorityMetaData, String.class);
	}
}
