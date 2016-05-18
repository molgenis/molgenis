package org.molgenis.auth;

import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.stereotype.Component;

@Component
public class AuthorityMetaData extends SystemEntityMetaDataImpl
{
	public static final String ENTITY_NAME = "authority";

	@Override
	public void init()
	{
		setName(ENTITY_NAME);
		setAbstract(true);
		addAttribute(Authority.ROLE).setLabel("role").setNillable(true);
	}
}
