package org.molgenis.data.mapper.meta;

import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.mapper.meta.MapperPackage.PACKAGE_MAPPER;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class MappingJobExecutionMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "MappingJobExecution";
	public static final String MAPPING_JOB_EXECUTION = PACKAGE_MAPPER + PACKAGE_SEPARATOR + SIMPLE_NAME;
	public static final String MAPPING_JOB_TYPE = "mapping";

	public static final String MAPPING_PROJECT_ID = "mappingProjectId";
	public static final String TARGET_ENTITY_TYPE_ID = "targetEntityTypeId";
	public static final String ADD_SOURCE_ATTRIBUTE = "addSourceAttribute";

	private final JobExecutionMetaData jobExecutionMetaData;

	@Autowired
	MappingJobExecutionMetadata(JobExecutionMetaData jobExecutionMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_MAPPER);
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
	}

	@Override
	public void init()
	{
		setLabel("Mapping job execution");
		setExtends(jobExecutionMetaData);

		addAttribute(MAPPING_PROJECT_ID).setDataType(STRING).setLabel("Mapping Project ID").setNillable(false);
		addAttribute(TARGET_ENTITY_TYPE_ID).setDataType(STRING).setLabel("Target Entity Type ID").setNillable(false);
		addAttribute(ADD_SOURCE_ATTRIBUTE).setDataType(BOOL).setLabel("Add source attribute").setNillable(false);
	}
}