package org.molgenis.script;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.jobs.model.JobPackage.PACKAGE_JOB;

@Component
public class ScriptJobExecutionMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "ScriptJobExecution";
	public static final String SCRIPT_JOB_EXECUTION = PACKAGE_JOB + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String NAME = "name";
	public static final String PARAMETERS = "parameters";

	private final JobExecutionMetaData jobExecutionMetaData;
	private final JobPackage jobPackage;

	ScriptJobExecutionMetadata(JobExecutionMetaData jobExecutionMetaData, JobPackage jobPackage)
	{
		super(SIMPLE_NAME, PACKAGE_JOB);
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
		this.jobPackage = requireNonNull(jobPackage);
	}

	@Override
	public void init()
	{
		setLabel("Script job execution");
		setExtends(jobExecutionMetaData);
		setPackage(jobPackage);
		addAttribute(NAME).setLabel("Name").setDescription("Name of the script to run").setNillable(false);
		addAttribute(PARAMETERS).setDataType(TEXT).setLabel("Parameter values").setNillable(false).setDefaultValue("");
	}
}
