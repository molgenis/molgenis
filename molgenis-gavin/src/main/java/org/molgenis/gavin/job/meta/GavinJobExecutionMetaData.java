package org.molgenis.gavin.job.meta;

import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.reindex.meta.IndexPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.reindex.meta.IndexPackage.PACKAGE_INDEX;

@Component
public class GavinJobExecutionMetaData extends SystemEntityMetaData
{
	private final JobExecutionMetaData jobExecutionMetaData;
	AttributeMetaDataFactory attributeMetaDataFactory;

	private static final String SIMPLE_NAME = "GavinJobExecution";
	public static final String GAVIN_JOB_EXECUTION = PACKAGE_INDEX + PACKAGE_SEPARATOR + SIMPLE_NAME;
	public static final String FILENAME = "filename";

	private final IndexPackage indexPackage;

	@Autowired
	public GavinJobExecutionMetaData(IndexPackage indexPackage, JobExecutionMetaData jobExecutionMetaData,
			AttributeMetaDataFactory attributeMetaDataFactory)
	{
		super(SIMPLE_NAME, PACKAGE_INDEX);
		this.indexPackage = requireNonNull(indexPackage);
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
		this.attributeMetaDataFactory = requireNonNull(attributeMetaDataFactory);
	}

	@Override
	public void init()
	{
		setLabel("Gavin job execution");
		setPackage(indexPackage);

		setExtends(jobExecutionMetaData);
		addAttribute(attributeMetaDataFactory.create().setName(FILENAME).setDataType(STRING).setNillable(false));
	}
}
