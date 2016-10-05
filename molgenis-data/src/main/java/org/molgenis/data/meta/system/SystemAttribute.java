package org.molgenis.data.meta.system;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetaDataMetaData;
import org.molgenis.data.support.BootstrapEntity;

public class SystemAttribute extends Attribute
{
	public SystemAttribute(AttributeMetaDataMetaData attrMetaMeta)
	{
		super(new BootstrapEntity(attrMetaMeta));
	}
}
