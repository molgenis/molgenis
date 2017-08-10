package org.molgenis.security.twofactor.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class UserSecretFactory extends AbstractSystemEntityFactory<UserSecret, UserSecretMetaData, String>
{
	UserSecretFactory(UserSecretMetaData userSecretMetaData, EntityPopulator entityPopulator)
	{
		super(UserSecret.class, userSecretMetaData, entityPopulator);
	}
}
