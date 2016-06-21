package org.molgenis.script;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScriptTypeFactory extends AbstractSystemEntityFactory<ScriptType, ScriptTypeMetaData, String>
{
	@Autowired
	ScriptTypeFactory(ScriptTypeMetaData scriptTypeMetaData)
	{
		super(ScriptType.class, scriptTypeMetaData);
	}
}
