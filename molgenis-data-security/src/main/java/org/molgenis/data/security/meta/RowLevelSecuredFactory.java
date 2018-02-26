package org.molgenis.data.security.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class RowLevelSecuredFactory
		extends AbstractSystemEntityFactory<RowLevelSecured, RowLevelSecuredMetadata, String>
{
	RowLevelSecuredFactory(RowLevelSecuredMetadata rowLevelSecuredMetadata, EntityPopulator entityPopulator)
	{
		super(RowLevelSecured.class, rowLevelSecuredMetadata, entityPopulator);
	}
}
