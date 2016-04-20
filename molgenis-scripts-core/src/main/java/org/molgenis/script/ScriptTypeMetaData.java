package org.molgenis.script;

import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.data.meta.EntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class ScriptTypeMetaData extends EntityMetaData
{
	public ScriptTypeMetaData()
	{
		super(ScriptType.ENTITY_NAME, ScriptType.class);
		addAttribute(ScriptParameter.NAME, ROLE_ID).setNillable(false);
	}
}
