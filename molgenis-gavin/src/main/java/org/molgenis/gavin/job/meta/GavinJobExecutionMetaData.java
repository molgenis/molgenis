package org.molgenis.gavin.job.meta;

import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.index.meta.IndexPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.index.meta.IndexPackage.PACKAGE_INDEX;

@Component
public class GavinJobExecutionMetaData extends SystemEntityMetaData
{
	private final JobExecutionMetaData jobExecutionMetaData;
	AttributeFactory attributeFactory;

	private static final String SIMPLE_NAME = "GavinJobExecution";
	public static final String GAVIN_JOB_EXECUTION = PACKAGE_INDEX + PACKAGE_SEPARATOR + SIMPLE_NAME;
	public static final String FILENAME = "filename";

	private final IndexPackage indexPackage;

	@Autowired
	public GavinJobExecutionMetaData(IndexPackage indexPackage, JobExecutionMetaData jobExecutionMetaData,
			AttributeFactory attributeFactory)
	{
		super(SIMPLE_NAME, PACKAGE_INDEX);
		this.indexPackage = requireNonNull(indexPackage);
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
		this.attributeFactory = requireNonNull(attributeFactory);
	}

	@Override
	public void init()
	{
		setLabel("Gavin job execution");
		setPackage(indexPackage);

		setExtends(jobExecutionMetaData);
		addAttribute(attributeFactory.create().setName(FILENAME).setDataType(STRING).setNillable(false));
	}
}
