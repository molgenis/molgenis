package org.molgenis.oneclickimporter.job;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.jobs.model.JobPackage.PACKAGE_JOB;

@Component
public class OneClickImportJobExecutionMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "OneClickImportJobExecution";

	public static final String FILE = "file";
	public static final String ENTITY_TYPES = "entityTypes";
	public static final String PACKAGE = "package";

	private final JobExecutionMetaData jobExecutionMetaData;
	private final JobPackage jobPackage;

	public static final String ONE_CLICK_IMPORT_JOB_TYPE = "OneClickImportJob";

	OneClickImportJobExecutionMetadata(JobExecutionMetaData jobExecutionMetaData, JobPackage jobPackage)
	{
		super(SIMPLE_NAME, PACKAGE_JOB);
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
		this.jobPackage = requireNonNull(jobPackage);
	}

	@Override
	public void init()
	{
		setLabel("One click import job execution");
		setExtends(jobExecutionMetaData);
		setPackage(jobPackage);

		addAttribute(FILE).setLabel("Imported file").setDescription("The file that was imported").setDataType(STRING);
		addAttribute(ENTITY_TYPES).setLabel("EntityTypes").setDescription("Imported EntityTypes").setDataType(STRING);
		addAttribute(PACKAGE).setLabel("Package name").setDataType(STRING);
	}
}
