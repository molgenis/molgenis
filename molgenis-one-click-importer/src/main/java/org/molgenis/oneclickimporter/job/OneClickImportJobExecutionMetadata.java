package org.molgenis.oneclickimporter.job;

import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.data.jobs.model.JobPackage;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.jobs.model.JobPackage.PACKAGE_JOB;
import static org.molgenis.data.meta.AttributeType.STRING;

@Component
public class OneClickImportJobExecutionMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "OneClickImportJobExecution";

	public static final String FILE = "file";
	public static final String ENTITY_TYPES = "entityTypes";
	public static final String PACKAGE = "package";

	private final EntityTypeMetadata entityTypeMetadata;
	private final JobExecutionMetaData jobExecutionMetaData;
	private final JobPackage jobPackage;

	public static final String ONE_CLICK_IMPORT_JOB_TYPE = "OneClickImportJob";

	@Autowired
	OneClickImportJobExecutionMetadata(EntityTypeMetadata entityTypeMetadata, JobExecutionMetaData jobExecutionMetaData,
			JobPackage jobPackage)
	{
		super(SIMPLE_NAME, PACKAGE_JOB);
		this.entityTypeMetadata = requireNonNull(entityTypeMetadata);
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
