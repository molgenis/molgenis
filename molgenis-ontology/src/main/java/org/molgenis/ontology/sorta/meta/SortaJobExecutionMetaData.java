package org.molgenis.ontology.sorta.meta;

import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.ontology.core.meta.OntologyPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.ontology.core.meta.OntologyPackage.PACKAGE_ONTOLOGY;

@Component
public class SortaJobExecutionMetaData extends SystemEntityMetaData
{
	public static final String SIMPLE_NAME = "SortaJobExecution";
	public static final String SORTA_JOB_EXECUTION = PACKAGE_ONTOLOGY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public final static String ONTOLOGY_IRI = "ontologyIri";
	public final static String NAME = "name";
	public final static String DELETE_URL = "deleteUrl";
	public final static String SOURCE_ENTITY = "sourceEntity";
	public final static String RESULT_ENTITY = "resultEntity";
	public final static String THRESHOLD = "Threshold";

	public static final String SORTA_MATCH_JOB_TYPE = "SORTA";

	private final OntologyPackage ontologyPackage;
	private final JobExecutionMetaData jobExecutionMetaData;

	@Autowired
	SortaJobExecutionMetaData(OntologyPackage ontologyPackage, JobExecutionMetaData jobExecutionMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_ONTOLOGY);
		this.ontologyPackage = requireNonNull(ontologyPackage);
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
	}

	@Override
	public void init()
	{
		setLabel("SORTA job execution");
		setPackage(ontologyPackage);

		setExtends(jobExecutionMetaData);
		addAttribute(NAME).setDataType(STRING).setNillable(false);
		addAttribute(RESULT_ENTITY).setDataType(STRING).setNillable(false);
		addAttribute(SOURCE_ENTITY).setDataType(STRING).setNillable(false);
		addAttribute(ONTOLOGY_IRI).setDataType(STRING).setNillable(false);
		addAttribute(DELETE_URL).setDataType(HYPERLINK).setNillable(false);
		addAttribute(THRESHOLD).setDataType(DECIMAL).setNillable(false);
	}
}