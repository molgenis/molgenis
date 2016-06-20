package org.molgenis.data.meta.system;

import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.support.BootstrapEntity;

public class SystemAttributeMetaData extends AttributeMetaData
{
	public SystemAttributeMetaData(AttributeMetaDataMetaData attrMetaMeta)
	{
		super(new BootstrapEntity(attrMetaMeta));
	}
}
