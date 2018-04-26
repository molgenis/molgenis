package org.molgenis.data.index.job;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.springframework.stereotype.Component;

import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.jobs.model.JobPackage.PACKAGE_JOB;

/**
 * This entity is used to track the progress of the execution of a IndexActionJob.
 */
@Component
public class IndexJobExecutionMeta extends SystemEntityType
{
	private static final String SIMPLE_NAME = "IndexJobExecution";
	public static final String INDEX_JOB_EXECUTION = PACKAGE_JOB + PACKAGE_SEPARATOR + SIMPLE_NAME;

	/**
	 * Example: Transaction id can be used to group all actions into one transaction.
	 */
	public static final String ID = "id";
	public static final String INDEX_ACTION_JOB_ID = "indexActionJobID";

	private final JobPackage jobPackage;
	private final JobExecutionMetaData jobExecutionMetaData;

	public IndexJobExecutionMeta(JobPackage jobPackage, JobExecutionMetaData jobExecutionMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_JOB);
		this.jobPackage = requireNonNull(jobPackage);
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
	}

	@Override
	public void init()
	{
		setLabel("Index job execution");
		setPackage(jobPackage);

		setExtends(jobExecutionMetaData);
		addAttribute(INDEX_ACTION_JOB_ID).setDescription(
				"ID of the IndexActionJob that contains the group of IndexActions that this index job execution will index.")
										 .setNillable(false);
	}

	@Override
	public Set<SystemEntityType> getDependencies()
	{
		return singleton(jobExecutionMetaData);
	}
}