package org.molgenis.genomebrowser.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class GenomeBrowserSettingsFactory
		extends AbstractSystemEntityFactory<GenomeBrowserSettings, GenomeBrowserSettingsMetadata, String>
{
	GenomeBrowserSettingsFactory(GenomeBrowserSettingsMetadata myEntityMeta, EntityPopulator entityPopulator)
	{
		super(GenomeBrowserSettings.class, myEntityMeta, entityPopulator);
	}
}