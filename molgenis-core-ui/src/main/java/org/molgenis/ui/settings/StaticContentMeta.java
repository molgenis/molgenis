package org.molgenis.ui.settings;

import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.data.meta.EntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class StaticContentMeta extends EntityMetaData
{
	public StaticContentMeta()
	{
		super(StaticContent.ENTITY_NAME, StaticContent.class);

		addAttribute(StaticContent.KEY, ROLE_ID).setLabel("Key");
		addAttribute(StaticContent.CONTENT).setDataType(TEXT).setLabel("Content");
	}
}