package org.molgenis.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserFactory extends AbstractSystemEntityFactory<User, UserMetaData, String>
{
	@Autowired
	UserFactory(UserMetaData userMetaData, EntityPopulator entityPopulator)
	{
		super(User.class, userMetaData, entityPopulator);
	}
}
