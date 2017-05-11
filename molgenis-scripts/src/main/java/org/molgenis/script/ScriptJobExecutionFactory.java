package org.molgenis.script;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScriptJobExecutionFactory
		extends AbstractSystemEntityFactory<ScriptJobExecution, ScriptJobExecutionMetadata, String>
{
	@Autowired
	ScriptJobExecutionFactory(ScriptJobExecutionMetadata scriptJobExecutionMetadata, EntityPopulator entityPopulator)
	{
		super(ScriptJobExecution.class, scriptJobExecutionMetadata, entityPopulator);
	}
}