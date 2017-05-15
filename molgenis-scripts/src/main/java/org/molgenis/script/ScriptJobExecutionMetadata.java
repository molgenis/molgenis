package org.molgenis.script;

import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class ScriptJobExecutionMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "ScriptJobExecution";
	public static final String SCRIPT_JOB_EXECUTION = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String NAME = "name";
	public static final String PARAMETERS = "parameters";

	private final JobExecutionMetaData jobExecutionMetaData;

	@Autowired
	ScriptJobExecutionMetadata(JobExecutionMetaData jobExecutionMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
	}

	@Override
	public void init()
	{
		setLabel("Script job execution");
		setExtends(jobExecutionMetaData);
		addAttribute(NAME).setLabel("Name").setDescription("Name of the script to run").setNillable(false);
		addAttribute(PARAMETERS).setDataType(TEXT).setLabel("Parameter values").setNillable(false).setDefaultValue("");
	}
}
