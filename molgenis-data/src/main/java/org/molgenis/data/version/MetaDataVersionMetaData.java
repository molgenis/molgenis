package org.molgenis.data.version;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultEntityMetaData;

//@Component
public class MetaDataVersionMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "MetaDataVersion";
	public static final String ATTR_VERSION = "version";

	public MetaDataVersionMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(ATTR_VERSION).setIdAttribute(true).setNillable(false).setDataType(MolgenisFieldTypes.INT);
	}

}
