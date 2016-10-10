package org.molgenis.data.annotation.web.meta;

import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class AnnotationJobExecutionMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "AnnotationJobExecution";
	public static final String ANNOTATION_JOB_EXECUTION = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String TARGET_NAME = "targetName";
	public static final String ANNOTATORS = "annotators";

	private final JobExecutionMetaData jobExecutionMetaData;

	@Autowired
	AnnotationJobExecutionMetaData(JobExecutionMetaData jobExecutionMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
	}

	@Override
	public void init()
	{
		setLabel("Annotation job execution");
		setExtends(jobExecutionMetaData);
		addAttribute(TARGET_NAME).setDataType(STRING).setLabel("target name")
				.setDescription("Fully qualified name of the entity that is being annotated.").setNillable(false);
		addAttribute(ANNOTATORS).setDataType(STRING).setLabel("Annotators run by this job").setNillable(false);
	}
}