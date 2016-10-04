package org.molgenis.script;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScriptParameterFactory
		extends AbstractSystemEntityFactory<ScriptParameter, ScriptParameterMetaData, String>
{
	@Autowired
	ScriptParameterFactory(ScriptParameterMetaData scriptParameterMetaData, EntityPopulator entityPopulator)
	{
		super(ScriptParameter.class, scriptParameterMetaData, entityPopulator);
	}
}
