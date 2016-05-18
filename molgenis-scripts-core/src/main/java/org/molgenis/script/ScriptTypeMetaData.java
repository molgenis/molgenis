package org.molgenis.script;

import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.stereotype.Component;

@Component
public class ScriptTypeMetaData extends SystemEntityMetaDataImpl
{
	@Override
	public void init()
	{
		setName(ScriptType.ENTITY_NAME);
		addAttribute(ScriptParameter.NAME, ROLE_ID).setNillable(false);
	}
}
