package org.molgenis.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserAuthorityFactory extends AbstractSystemEntityFactory<UserAuthority, UserAuthorityMetaData, String>
{
	@Autowired
	UserAuthorityFactory(UserAuthorityMetaData userAuthorityMetaData, EntityPopulator entityPopulator)
	{
		super(UserAuthority.class, userAuthorityMetaData, entityPopulator);
	}
}
