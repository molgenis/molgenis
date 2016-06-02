package org.molgenis.data.system;

import org.molgenis.data.AbstractEntityFactory;
import org.molgenis.data.meta.system.ImportRunMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ImportRunFactory extends AbstractEntityFactory<ImportRun, ImportRunMetaData, String>
{
	@Autowired
	ImportRunFactory(ImportRunMetaData importRunMetaData)
	{
		super(ImportRun.class, importRunMetaData, String.class);
	}
}
