package org.molgenis.data.system;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.data.meta.system.ImportRunMetaData;
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
