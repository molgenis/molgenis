package org.molgenis.script;

import org.molgenis.data.AbstractEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScriptTypeFactory extends AbstractEntityFactory<ScriptType, ScriptTypeMetaData, String>
{
	@Autowired
	ScriptTypeFactory(ScriptTypeMetaData scriptTypeMetaData)
	{
		super(ScriptType.class, scriptTypeMetaData, String.class);
	}
}
