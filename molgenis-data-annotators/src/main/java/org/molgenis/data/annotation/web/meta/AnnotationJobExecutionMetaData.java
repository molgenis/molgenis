package org.molgenis.data.annotation.web.meta;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.jobs.model.JobPackage.PACKAGE_JOB;

@Component
public class AnnotationJobExecutionMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "AnnotationJobExecution";
	public static final String ANNOTATION_JOB_EXECUTION = PACKAGE_JOB + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String TARGET_NAME = "targetName";
	public static final String ANNOTATORS = "annotators";

	private final JobExecutionMetaData jobExecutionMetaData;
	private final JobPackage jobPackage;

	AnnotationJobExecutionMetaData(JobExecutionMetaData jobExecutionMetaData, JobPackage jobPackage)
	{
		super(SIMPLE_NAME, PACKAGE_JOB);
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
		this.jobPackage = requireNonNull(jobPackage);
	}

	@Override
	public void init()
	{
		setLabel("Annotation job execution");
		setExtends(jobExecutionMetaData);
		setPackage(jobPackage);
		addAttribute(TARGET_NAME).setDataType(STRING)
								 .setLabel("target name")
								 .setDescription("Fully qualified name of the entity that is being annotated.")
								 .setNillable(false);
		addAttribute(ANNOTATORS).setDataType(STRING).setLabel("Annotators run by this job").setNillable(false);
	}
}