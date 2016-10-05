package org.molgenis.data.meta.system;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.support.BootstrapEntity;

public class SystemAttribute extends Attribute
{
	public SystemAttribute(AttributeMetaData attrMetaMeta)
	{
		super(new BootstrapEntity(attrMetaMeta));
	}
}
