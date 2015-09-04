package org.molgenis.script;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class ScriptParameterMetaData extends DefaultEntityMetaData
{
	public ScriptParameterMetaData()
	{
		super(ScriptParameter.ENTITY_NAME, ScriptParameter.class);
		addAttribute(ScriptParameter.NAME).setIdAttribute(true).setNillable(false);
	}
}
