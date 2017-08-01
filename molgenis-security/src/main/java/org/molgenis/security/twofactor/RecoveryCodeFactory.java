package org.molgenis.security.twofactor;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RecoveryCodeFactory extends AbstractSystemEntityFactory<RecoveryCode, RecoveryCodeMetadata, String>
{
	@Autowired
	RecoveryCodeFactory(RecoveryCodeMetadata recoveryCodeMetadata, EntityPopulator entityPopulator)
	{
		super(RecoveryCode.class, recoveryCodeMetadata, entityPopulator);
	}
}