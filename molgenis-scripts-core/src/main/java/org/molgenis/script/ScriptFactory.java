package org.molgenis.script;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScriptFactory extends AbstractSystemEntityFactory<Script, ScriptMetaData, String>
{
	@Autowired
	ScriptFactory(ScriptMetaData scriptMetaData)
	{
		super(Script.class, scriptMetaData, String.class);
	}
}
