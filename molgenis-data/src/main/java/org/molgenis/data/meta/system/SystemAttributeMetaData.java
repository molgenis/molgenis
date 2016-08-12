package org.molgenis.data.meta.system;

import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataMetaData;
import org.molgenis.data.support.BootstrapEntity;

public class SystemAttributeMetaData extends AttributeMetaData
{
	public SystemAttributeMetaData(AttributeMetaDataMetaData attrMetaMeta)
	{
		super(new BootstrapEntity(attrMetaMeta));
	}
}
