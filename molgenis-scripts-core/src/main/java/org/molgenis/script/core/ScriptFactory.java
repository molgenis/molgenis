package org.molgenis.script.core;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class ScriptFactory extends AbstractSystemEntityFactory<Script, ScriptMetaData, String>
{
	ScriptFactory(ScriptMetaData scriptMetaData, EntityPopulator entityPopulator)
	{
		super(Script.class, scriptMetaData, entityPopulator);
	}
}
