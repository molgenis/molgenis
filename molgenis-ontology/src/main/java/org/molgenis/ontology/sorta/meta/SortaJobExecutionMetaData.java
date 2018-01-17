package org.molgenis.ontology.sorta.meta;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.jobs.model.JobPackage.PACKAGE_JOB;

@Component
public class SortaJobExecutionMetaData extends SystemEntityType
{
	public static final String SIMPLE_NAME = "SortaJobExecution";
	public static final String SORTA_JOB_EXECUTION = PACKAGE_JOB + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ONTOLOGY_IRI = "ontologyIri";
	public static final String NAME = "name";
	public final static String DELETE_URL = "deleteUrl";
	public final static String SOURCE_ENTITY = "sourceEntity";
	public final static String RESULT_ENTITY = "resultEntity";
	public final static String THRESHOLD = "Threshold";

	public static final String SORTA_MATCH_JOB_TYPE = "SORTA";

	private final JobPackage jobPackage;
	private final JobExecutionMetaData jobExecutionMetaData;

	SortaJobExecutionMetaData(JobPackage jobPackage, JobExecutionMetaData jobExecutionMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_JOB);
		this.jobPackage = requireNonNull(jobPackage);
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
	}

	@Override
	public void init()
	{
		setLabel("SORTA job execution");
		setPackage(jobPackage);

		setExtends(jobExecutionMetaData);
		addAttribute(NAME).setDataType(STRING).setNillable(false);
		addAttribute(RESULT_ENTITY).setDataType(STRING).setNillable(false);
		addAttribute(SOURCE_ENTITY).setDataType(STRING).setNillable(false);
		addAttribute(ONTOLOGY_IRI).setDataType(STRING).setNillable(false);
		addAttribute(DELETE_URL).setDataType(HYPERLINK).setNillable(false);
		addAttribute(THRESHOLD).setDataType(DECIMAL).setNillable(false);
	}
}