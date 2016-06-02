package org.molgenis.script;

import org.molgenis.data.AbstractEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScriptParameterFactory extends AbstractEntityFactory<ScriptParameter, ScriptParameterMetaData, String>
{
	@Autowired
	ScriptParameterFactory(ScriptParameterMetaData scriptParameterMetaData)
	{
		super(ScriptParameter.class, scriptParameterMetaData, String.class);
	}
}
