package org.molgenis.script;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
class ScriptJobExecutionFactory
		extends AbstractSystemEntityFactory<ScriptJobExecution, ScriptJobExecutionMetadata, String>
{
	ScriptJobExecutionFactory(ScriptJobExecutionMetadata scriptJobExecutionMetadata, EntityPopulator entityPopulator)
	{
		super(ScriptJobExecution.class, scriptJobExecutionMetadata, entityPopulator);
	}
}