package org.molgenis.security.twofactor;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class UserSecretFactory extends AbstractSystemEntityFactory<UserSecret, UserSecretMetaData, String>
{

	@Autowired
	UserSecretFactory(UserSecretMetaData userSecretMetaData, EntityPopulator entityPopulator)
	{
		super(UserSecret.class, userSecretMetaData, entityPopulator);
	}

}
