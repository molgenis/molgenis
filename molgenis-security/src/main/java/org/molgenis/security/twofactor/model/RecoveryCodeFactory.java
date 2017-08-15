package org.molgenis.security.twofactor.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class RecoveryCodeFactory extends AbstractSystemEntityFactory<RecoveryCode, RecoveryCodeMetadata, String>
{
	RecoveryCodeFactory(RecoveryCodeMetadata recoveryCodeMetadata, EntityPopulator entityPopulator)
	{
		super(RecoveryCode.class, recoveryCodeMetadata, entityPopulator);
	}
}