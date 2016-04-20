package org.molgenis.script;

import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.data.meta.EntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class ScriptParameterMetaData extends EntityMetaData
{
	public ScriptParameterMetaData()
	{
		super(ScriptParameter.ENTITY_NAME, ScriptParameter.class);
		addAttribute(ScriptParameter.NAME, ROLE_ID).setNillable(false);
	}
}
