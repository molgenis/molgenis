package org.molgenis.data.mapper.job;

import org.molgenis.data.Entity;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.meta.model.EntityType;

import static org.molgenis.data.mapper.job.MappingJobExecutionMetadata.*;

public class MappingJobExecution extends JobExecution
{
	public MappingJobExecution(Entity entity)
	{
		super(entity);
		setType(MappingJobExecutionMetadata.MAPPING_JOB_TYPE);
	}

	public MappingJobExecution(EntityType entityType)
	{
		super(entityType);
		setType(MappingJobExecutionMetadata.MAPPING_JOB_TYPE);
	}

	public MappingJobExecution(String identifier, EntityType entityType)
	{
		super(identifier, entityType);
		setType(MappingJobExecutionMetadata.MAPPING_JOB_TYPE);
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

	public Boolean isAddSourceAttribute()
	{
		return getBoolean(ADD_SOURCE_ATTRIBUTE);
	}

	public void setAddSourceAttribute(Boolean addSourceAttribute)
	{
		set(ADD_SOURCE_ATTRIBUTE, addSourceAttribute);
	}

	public String getPackageId()
	{
		return getString(PACKAGE_ID);
	}

	public void setPackageId(String packageId)
	{
		set(PACKAGE_ID, packageId);
	}

	public String getLabel()
	{
		return getString(LABEL);
	}

	public void setLabel(String label)
	{
		set(LABEL, label);
	}
}
