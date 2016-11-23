package org.molgenis.data.importer;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ImportRunFactory extends AbstractSystemEntityFactory<ImportRun, ImportRunMetaData, String>
{
	@Autowired
	ImportRunFactory(ImportRunMetaData importRunMetaData, EntityPopulator entityPopulator)
	{
		super(ImportRun.class, importRunMetaData, entityPopulator);
	}
}
