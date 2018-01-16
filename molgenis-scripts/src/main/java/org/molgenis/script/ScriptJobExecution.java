package org.molgenis.script;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;

import static org.molgenis.script.ScriptJobExecutionMetadata.NAME;
import static org.molgenis.script.ScriptJobExecutionMetadata.PARAMETERS;

public class ScriptJobExecution extends JobExecution
{
	public ScriptJobExecution(Entity entity)
	{
		super(entity);
		setType("Script");
	}

	public ScriptJobExecution(EntityType entityType)
	{
		super(entityType);
		setType("Script");
	}

	public ScriptJobExecution(String identifier, EntityType entityType)
	{
		super(identifier, entityType);
		setType("Script");
	}

	public void setName(String name)
	{
		set(NAME, name);
	}

	public String getName()
	{
		return getString(NAME);
	}

	public String getParameters()
	{
		return getString(PARAMETERS);
	}

	public void setParameters(String parameters)
	{
		set(PARAMETERS, parameters);
	}
}
