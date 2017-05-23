package org.molgenis.data.jobs.model.hello;

import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.data.jobs.model.JobPackage;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.jobs.model.JobPackage.PACKAGE_JOB;
import static org.molgenis.data.meta.AttributeType.INT;

/**
 * Metadata for {@link HelloWorldJobExecution}s.
 */
@Component
public class HelloWorldJobExecutionMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "HelloWorldJobExecution";
	static final String DELAY = "delay";

	private final JobExecutionMetaData jobExecutionMetaData;
	private final JobPackage jobPackage;

	@Autowired
	HelloWorldJobExecutionMetadata(JobExecutionMetaData jobExecutionMetaData, JobPackage jobPackage)
	{
		super(SIMPLE_NAME, PACKAGE_JOB);
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
		this.jobPackage = requireNonNull(jobPackage);
	}

	@Override
	public void init()
	{
		setLabel("Hello World Job Execution");
		setExtends(jobExecutionMetaData);
		setPackage(jobPackage);
		addAttribute(DELAY).setLabel("Delay").setDescription("Delay in seconds").setDataType(INT).setNillable(false);
	}
}
