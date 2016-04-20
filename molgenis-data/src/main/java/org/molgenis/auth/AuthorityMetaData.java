package org.molgenis.auth;

import org.molgenis.data.meta.EntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class AuthorityMetaData extends EntityMetaData
{

	public AuthorityMetaData()
	{
		super("authority");
		setAbstract(true);
		addAttribute(Authority.ROLE).setLabel("role").setNillable(true);
	}
}
