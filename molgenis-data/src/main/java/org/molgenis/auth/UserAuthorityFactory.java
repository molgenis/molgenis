package org.molgenis.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserAuthorityFactory extends AbstractSystemEntityFactory<UserAuthority, UserAuthorityMetaData, String>
{
	@Autowired
	UserAuthorityFactory(UserAuthorityMetaData userAuthorityMetaData)
	{
		super(UserAuthority.class, userAuthorityMetaData, String.class);
	}
}
