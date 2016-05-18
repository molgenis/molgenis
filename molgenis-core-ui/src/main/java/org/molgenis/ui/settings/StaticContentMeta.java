package org.molgenis.ui.settings;

import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.stereotype.Component;

@Component
class StaticContentMeta extends SystemEntityMetaDataImpl
{
	@Override
	public void init()
	{
		setName(StaticContent.ENTITY_NAME);
		addAttribute(StaticContent.KEY, ROLE_ID).setLabel("Key");
		addAttribute(StaticContent.CONTENT).setDataType(TEXT).setLabel("Content");
	}
}