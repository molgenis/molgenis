package org.molgenis.ontology.sorta.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.DECIMAL;
import static org.molgenis.MolgenisFieldTypes.HYPERLINK;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;
import static org.molgenis.ontology.core.model.OntologyPackage.PACKAGE_ONTOLOGY;

import org.molgenis.data.jobs.JobExecutionMetaData;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SortaJobExecutionMetaData extends SystemEntityMetaDataImpl
{
	public final static String SIMPLE_NAME = "SortaJobExecution";
	public static final String SORTA_JOB_EXECUTION = PACKAGE_ONTOLOGY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public final static String ONTOLOGY_IRI = "ontologyIri";
	public final static String NAME = "name";
	public final static String DELETE_URL = "deleteUrl";
	public final static String SOURCE_ENTITY = "sourceEntity";
	public final static String RESULT_ENTITY = "resultEntity";
	public final static String THRESHOLD = "Threshold";

	public static final String SORTA_MATCH_JOB_TYPE = "SORTA";

	private final OntologyPackage ontologyPackage;
	private JobExecutionMetaData jobExecutionMetaData;

	@Autowired
	SortaJobExecutionMetaData(OntologyPackage ontologyPackage)
	{
		super(SIMPLE_NAME, PACKAGE_ONTOLOGY);
		this.ontologyPackage = requireNonNull(ontologyPackage);
	}

	@Override
	public void init()
	{
		setPackage(ontologyPackage);

		setExtends(jobExecutionMetaData);
		addAttribute(NAME).setDataType(STRING).setNillable(false);
		addAttribute(RESULT_ENTITY).setDataType(STRING).setNillable(false);
		addAttribute(SOURCE_ENTITY).setDataType(STRING).setNillable(false);
		addAttribute(ONTOLOGY_IRI).setDataType(STRING).setNillable(false);
		addAttribute(DELETE_URL).setDataType(HYPERLINK).setNillable(false);
		addAttribute(THRESHOLD).setDataType(DECIMAL).setNillable(false);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setJobExecutionMetaData(JobExecutionMetaData jobExecutionMetaData)
	{
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
	}
}