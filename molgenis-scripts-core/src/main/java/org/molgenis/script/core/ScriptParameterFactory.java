package org.molgenis.script.core;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class ScriptParameterFactory
		extends AbstractSystemEntityFactory<ScriptParameter, ScriptParameterMetaData, String>
{
	ScriptParameterFactory(ScriptParameterMetaData scriptParameterMetaData, EntityPopulator entityPopulator)
	{
		super(ScriptParameter.class, scriptParameterMetaData, entityPopulator);
	}
}
