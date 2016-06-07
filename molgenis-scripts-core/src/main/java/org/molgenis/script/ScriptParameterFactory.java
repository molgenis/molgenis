package org.molgenis.script;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScriptParameterFactory
		extends AbstractSystemEntityFactory<ScriptParameter, ScriptParameterMetaData, String>
{
	@Autowired
	ScriptParameterFactory(ScriptParameterMetaData scriptParameterMetaData)
	{
		super(ScriptParameter.class, scriptParameterMetaData, String.class);
	}
}
