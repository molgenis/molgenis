package org.molgenis.data.jobs.model.hello;

import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;


/**
 * Metadata for {@link HelloWorldJobExecution}s.
 */
@Component
public class HelloWorldJobExecutionMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "HelloWorldJobExecution";
	static final String DELAY = "delay";

	private final JobExecutionMetaData jobExecutionMetaData;

	@Autowired
	HelloWorldJobExecutionMetadata(JobExecutionMetaData jobExecutionMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
	}

	@Override
	public void init()
	{
		setLabel("Hello World Job Execution");
		setExtends(jobExecutionMetaData);
		addAttribute(DELAY).setLabel("Delay").setDescription("Delay in seconds").setDataType(INT).setNillable(false);
	}
}
