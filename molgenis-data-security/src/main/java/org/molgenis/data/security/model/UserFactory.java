package org.molgenis.data.security.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class UserFactory extends AbstractSystemEntityFactory<UserEntity, UserMetadata, String>
{
	UserFactory(UserMetadata userMetaData, EntityPopulator entityPopulator)
	{
		super(UserEntity.class, userMetaData, entityPopulator);
	}
}
