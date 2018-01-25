package org.molgenis.data.security.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class UserFactory extends AbstractSystemEntityFactory<User, UserMetaData, String>
{
	UserFactory(UserMetaData userMetaData, EntityPopulator entityPopulator)
	{
		super(User.class, userMetaData, entityPopulator);
	}
}
