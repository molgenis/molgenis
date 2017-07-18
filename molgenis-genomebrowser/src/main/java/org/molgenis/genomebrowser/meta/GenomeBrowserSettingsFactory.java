package org.molgenis.genomebrowser.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GenomeBrowserSettingsFactory
		extends AbstractSystemEntityFactory<GenomeBrowserSettings, GenomeBrowserSettingsMetadata, String>
{
	@Autowired
	GenomeBrowserSettingsFactory(GenomeBrowserSettingsMetadata myEntityMeta, EntityPopulator entityPopulator)
	{
		super(GenomeBrowserSettings.class, myEntityMeta, entityPopulator);
	}
}