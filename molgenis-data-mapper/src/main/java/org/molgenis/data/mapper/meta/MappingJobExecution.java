package org.molgenis.data.mapper.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.meta.model.EntityType;

import static org.molgenis.data.mapper.meta.MappingJobExecutionMetadata.*;

public class MappingJobExecution extends JobExecution
{
	public MappingJobExecution(Entity entity)
	{
		super(entity);
	}

	public MappingJobExecution(EntityType entityType)
	{
		super(entityType);
		setDefaultValues();
	}

	public MappingJobExecution(String identifier, EntityType entityType)
	{
		super(identifier, entityType);
		setDefaultValues();
	}

	public String getMappingProjectId()
	{
		return getString(MAPPING_PROJECT_ID);
	}

	public void setMappingProjectId(String mappingProjectId)
	{
		set(MAPPING_PROJECT_ID, mappingProjectId);
	}

	public String getTargetEntityTypeId()
	{
		return getString(TARGET_ENTITY_TYPE_ID);
	}

	public void setTargetEntityTypeId(String targetEntityTypeId)
	{
		set(TARGET_ENTITY_TYPE_ID, targetEntityTypeId);
	}

	public String isAddSourceAttribute()
	{
		return getString(ADD_SOURCE_ATTRIBUTE);
	}

	public void setAddSourceAttribute(boolean addSourceAttribute)
	{
		set(ADD_SOURCE_ATTRIBUTE, addSourceAttribute);
	}

	private void setDefaultValues()
	{
		setType(MAPPING_JOB_TYPE);
	}
}
