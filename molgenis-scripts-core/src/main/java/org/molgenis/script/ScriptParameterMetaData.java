package org.molgenis.script;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class ScriptParameterMetaData extends DefaultEntityMetaData
{
	public ScriptParameterMetaData()
	{
		super(ScriptParameter.ENTITY_NAME, ScriptParameter.class);
		addAttribute(ScriptParameter.NAME, ROLE_ID).setNillable(false);
	}
}
