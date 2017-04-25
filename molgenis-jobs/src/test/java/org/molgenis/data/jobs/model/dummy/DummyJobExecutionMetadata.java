package org.molgenis.data.jobs.model.dummy;

import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class DummyJobExecutionMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "DummyJobExecution";

	private final JobExecutionMetaData jobExecutionMetaData;

	@Autowired
	DummyJobExecutionMetadata(JobExecutionMetaData jobExecutionMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
	}

	@Override
	public void init()
	{
		setLabel("Dummy Job Execution");
		setExtends(jobExecutionMetaData);
	}
}
