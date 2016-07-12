package org.molgenis.data.annotation.core.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.meta.model.EntityMetaData;

import static org.molgenis.data.annotation.core.meta.AnnotationJobExecutionMetaData.ANNOTATORS;
import static org.molgenis.data.annotation.core.meta.AnnotationJobExecutionMetaData.TARGET_NAME;

public class AnnotationJobExecution extends JobExecution
{
	public AnnotationJobExecution(Entity entity)
	{
		super(entity);
	}

	public AnnotationJobExecution(EntityMetaData entityMeta)
	{
		super(entityMeta);
		setDefaultValues();
	}

	public AnnotationJobExecution(String identifier, EntityMetaData entityMeta)
	{
		super(identifier, entityMeta);
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
