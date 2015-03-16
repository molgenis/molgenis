package org.molgenis.data.version;

import org.molgenis.data.DataService;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;

public class UpgradeFrom0To1 extends MetaDataUpgrade
{

	public UpgradeFrom0To1(DataService dataService)
	{
		super(0, 1, dataService);
	}

	@Override
	public void upgrade(ManageableRepositoryCollection defaultBackend)
	{
		// Add expression attribute to AttributeMetaData
		defaultBackend.addAttribute(AttributeMetaDataMetaData.ENTITY_NAME,
				new AttributeMetaDataMetaData().getAttribute(AttributeMetaDataMetaData.EXPRESSION));

		// Add backend attribute to EntityMetaData
		defaultBackend.addAttribute(EntityMetaDataMetaData.ENTITY_NAME,
				new EntityMetaDataMetaData().getAttribute(EntityMetaDataMetaData.BACKEND));
	}
}
