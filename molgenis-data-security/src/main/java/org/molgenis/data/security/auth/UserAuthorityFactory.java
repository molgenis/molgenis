package org.molgenis.data.security.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class UserAuthorityFactory extends AbstractSystemEntityFactory<UserAuthority, UserAuthorityMetaData, String>
{
	UserAuthorityFactory(UserAuthorityMetaData userAuthorityMetaData, EntityPopulator entityPopulator)
	{
		super(UserAuthority.class, userAuthorityMetaData, entityPopulator);
	}
}
