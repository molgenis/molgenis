package org.molgenis.semanticmapper.job;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.jobs.model.JobPackage.PACKAGE_JOB;

@Component
public class MappingJobExecutionMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "MappingJobExecution";
	static final String MAPPING_JOB_EXECUTION = PACKAGE_JOB + PACKAGE_SEPARATOR + SIMPLE_NAME;
	static final String MAPPING_JOB_TYPE = "mapping";

	static final String MAPPING_PROJECT_ID = "mappingProjectId";
	static final String TARGET_ENTITY_TYPE_ID = "targetEntityTypeId";
	static final String ADD_SOURCE_ATTRIBUTE = "addSourceAttribute";
	static final String PACKAGE_ID = "packageId";
	static final String LABEL = "label";

	private final JobExecutionMetaData jobExecutionMetaData;
	private final JobPackage jobPackage;

	MappingJobExecutionMetadata(JobExecutionMetaData jobExecutionMetaData, JobPackage jobPackage)
	{
		super(SIMPLE_NAME, PACKAGE_JOB);
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
		this.jobPackage = requireNonNull(jobPackage);
	}

	@Override
	public void init()
	{
		setLabel("Mapping Job Execution");
		setExtends(jobExecutionMetaData);
		setPackage(jobPackage);
		addAttribute(MAPPING_PROJECT_ID).setDataType(STRING).setLabel("Mapping Project ID").setNillable(false);
		addAttribute(TARGET_ENTITY_TYPE_ID).setDataType(STRING).setLabel("Target Entity Type ID").setNillable(false);
		addAttribute(ADD_SOURCE_ATTRIBUTE).setDataType(BOOL)
										  .setLabel("Add source attribute")
										  .setNillable(true)
										  .setDescription(
												  "If the target entity type should have a 'source' attribute. Ignored when mapping to an existing entity type");
		addAttribute(PACKAGE_ID).setDataType(STRING)
								.setLabel("Package")
								.setNillable(true)
								.setDescription(
										"The destination package of the target entity type. Ignored when mapping to an existing entity type.");
		addAttribute(LABEL).setDataType(STRING)
						   .setLabel("Label")
						   .setNillable(true)
						   .setDescription(
								   "The label of the target entity type. Ignored when mapping to an existing entity type.");
	}
}