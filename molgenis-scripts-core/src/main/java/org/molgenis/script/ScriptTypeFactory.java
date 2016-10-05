package org.molgenis.script;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScriptTypeFactory extends AbstractSystemEntityFactory<ScriptType, ScriptTypeMetaData, String>
{
	@Autowired
	ScriptTypeFactory(ScriptTypeMetaData scriptTypeMetaData, EntityPopulator entityPopulator)
	{
		super(ScriptType.class, scriptTypeMetaData, entityPopulator);
	}
}
