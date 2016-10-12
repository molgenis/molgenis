package org.molgenis.data.elasticsearch.index.job;

import org.molgenis.data.index.meta.IndexPackage;
import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.index.meta.IndexPackage.PACKAGE_INDEX;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

/**
 * This entity is used to track the progress of the execution of a IndexActionJob.
 */
@Component
public class IndexJobExecutionMeta extends SystemEntityType
{
	private static final String SIMPLE_NAME = "IndexJobExecution";
	public static final String INDEX_JOB_EXECUTION = PACKAGE_INDEX + PACKAGE_SEPARATOR + SIMPLE_NAME;

	/**
	 * Example: Transaction id can be used to group all actions into one transaction.
	 */
	public static final String ID = "id";
	public static final String INDEX_ACTION_JOB_ID = "indexActionJobID";

	private final IndexPackage indexPackage;
	private final JobExecutionMetaData jobExecutionMetaData;

	@Autowired
	public IndexJobExecutionMeta(IndexPackage indexPackage, JobExecutionMetaData jobExecutionMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_INDEX);
		this.indexPackage = requireNonNull(indexPackage);
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
	}

	@Override
	public void init()
	{
		setLabel("Index job execution");
		setPackage(indexPackage);

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