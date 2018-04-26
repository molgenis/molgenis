package org.molgenis.gavin.job.meta;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.jobs.model.JobPackage.PACKAGE_JOB;

@Component
public class GavinJobExecutionMetaData extends SystemEntityType
{
	private final JobExecutionMetaData jobExecutionMetaData;

	private static final String SIMPLE_NAME = "GavinJobExecution";
	public static final String GAVIN_JOB_EXECUTION = PACKAGE_JOB + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String FILENAME = "filename";
	public static final String INPUT_FILE_EXTENSION = "extension";
	public static final String COMMENTS = "comments";
	public static final String CADDS = "cadds";
	public static final String VCFS = "vcfs";
	public static final String ERRORS = "errors";
	public static final String SKIPPEDS = "skippeds";
	public static final String INDELS_NOCADD = "indels";

	private final JobPackage jobPackage;

	public GavinJobExecutionMetaData(JobPackage jobPackage, JobExecutionMetaData jobExecutionMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_JOB);
		this.jobPackage = requireNonNull(jobPackage);
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
	}

	@Override
	public void init()
	{
		setLabel("Gavin job execution");
		setPackage(jobPackage);
		setExtends(jobExecutionMetaData);
		addAttribute(FILENAME).setDataType(STRING).setNillable(false);
		addAttribute(INPUT_FILE_EXTENSION).setDataType(STRING).setNillable(false);
		addAttribute(COMMENTS).setDataType(INT).setNillable(true);
		addAttribute(CADDS).setDataType(INT).setNillable(true);
		addAttribute(VCFS).setDataType(INT).setNillable(true);
		addAttribute(ERRORS).setDataType(INT).setNillable(true);
		addAttribute(INDELS_NOCADD).setDataType(INT).setNillable(true);
		addAttribute(SKIPPEDS).setDataType(INT).setNillable(true);
	}
}
