package org.molgenis.auth;

import org.molgenis.data.AbstractEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserAuthorityFactory extends AbstractEntityFactory<UserAuthority, UserAuthorityMetaData, String>
{
	@Autowired
	UserAuthorityFactory(UserAuthorityMetaData userAuthorityMetaData)
	{
		super(UserAuthority.class, userAuthorityMetaData, String.class);
	}
}
