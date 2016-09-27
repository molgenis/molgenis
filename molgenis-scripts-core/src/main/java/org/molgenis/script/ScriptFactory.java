package org.molgenis.script;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScriptFactory extends AbstractSystemEntityFactory<Script, ScriptMetaData, String>
{
	@Autowired
	ScriptFactory(ScriptMetaData scriptMetaData, EntityPopulator entityPopulator)
	{
		super(Script.class, scriptMetaData, entityPopulator);
	}
}
