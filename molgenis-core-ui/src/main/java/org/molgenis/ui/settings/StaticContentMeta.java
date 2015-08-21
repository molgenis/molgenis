package org.molgenis.ui.settings;

import static org.molgenis.MolgenisFieldTypes.TEXT;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class StaticContentMeta extends DefaultEntityMetaData
{
	public StaticContentMeta()
	{
		super(StaticContent.ENTITY_NAME, StaticContent.class);

		addAttribute(StaticContent.KEY).setIdAttribute(true).setNillable(false).setLabel("Key");
		addAttribute(StaticContent.CONTENT).setDataType(TEXT).setLabel("Content");
	}
}