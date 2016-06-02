package org.molgenis.data.annotation.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.meta.RootSystemPackage.PACKAGE_SYSTEM;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.jobs.JobExecutionMetaData;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AnnotationJobExecutionMetaData extends SystemEntityMetaDataImpl
{
	public static final String SIMPLE_NAME = "AnnotationJobExecution";
	public static final String ANNOTATION_JOB_EXECUTION = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String TARGET_NAME = "targetName";
	public static final String ANNOTATORS = "annotators";

	private JobExecutionMetaData jobExecutionMetaData;

	AnnotationJobExecutionMetaData()
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
	}

	@Override
	public void init()
	{
		setExtends(jobExecutionMetaData);
		addAttribute(TARGET_NAME).setDataType(MolgenisFieldTypes.STRING).setLabel("target name")
				.setDescription("Fully qualified name of the entity that is being annotated.").setNillable(false);
		addAttribute(ANNOTATORS).setDataType(MolgenisFieldTypes.STRING).setLabel("Annotators run by this job")
				.setNillable(false);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setJobExecutionMetaData(JobExecutionMetaData jobExecutionMetaData)
	{
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
	}
}