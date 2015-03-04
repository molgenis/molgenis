package org.molgenis.script;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class ScriptTypeMetaData extends DefaultEntityMetaData
{

	public ScriptTypeMetaData()
	{
		super(ScriptType.ENTITY_NAME, ScriptType.class);
		addAttribute(ScriptParameter.NAME).setIdAttribute(true).setNillable(false);
	}

}
