package org.molgenis.data.annotation.web.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;

import static org.molgenis.data.annotation.web.meta.AnnotationJobExecutionMetaData.ANNOTATORS;
import static org.molgenis.data.annotation.web.meta.AnnotationJobExecutionMetaData.TARGET_NAME;

public class AnnotationJobExecution extends JobExecution
{
	public AnnotationJobExecution(Entity entity)
	{
		super(entity);
	}

	public AnnotationJobExecution(EntityType entityType)
	{
		super(entityType);
		setDefaultValues();
	}

	public AnnotationJobExecution(String identifier, EntityType entityType)
	{
		super(identifier, entityType);
		setDefaultValues();
	}

	public String getTargetName()
	{
		return getString(TARGET_NAME);
	}

	public void setTargetName(String value)
	{
		set(TARGET_NAME, value);
	}

	public String getAnnotators()
	{
		return getString(ANNOTATORS);
	}

	public void setAnnotators(String value)
	{
		set(ANNOTATORS, value);
	}

	private void setDefaultValues()
	{
		setType(ANNOTATORS);
	}
}
