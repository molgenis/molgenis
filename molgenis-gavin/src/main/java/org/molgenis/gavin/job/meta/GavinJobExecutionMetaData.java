package org.molgenis.gavin.job.meta;

import org.molgenis.data.index.meta.IndexPackage;
import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.index.meta.IndexPackage.PACKAGE_INDEX;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class GavinJobExecutionMetaData extends SystemEntityType
{
	private final JobExecutionMetaData jobExecutionMetaData;

	private static final String SIMPLE_NAME = "GavinJobExecution";
	public static final String GAVIN_JOB_EXECUTION = PACKAGE_INDEX + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String FILENAME = "filename";
	public static final String INPUT_FILE_EXTENSION = "extension";
	public static final String COMMENTS = "comments";
	public static final String CADDS = "cadds";
	public static final String VCFS = "vcfs";
	public static final String ERRORS = "errors";
	public static final String SKIPPEDS = "skippeds";
	public static final String INDELS_NOCADD = "indels";

	private final IndexPackage indexPackage;

	@Autowired
	public GavinJobExecutionMetaData(IndexPackage indexPackage, JobExecutionMetaData jobExecutionMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_INDEX);
		this.indexPackage = requireNonNull(indexPackage);
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
	}

	@Override
	public void init()
	{
		setLabel("Gavin job execution");
		setPackage(indexPackage);
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
