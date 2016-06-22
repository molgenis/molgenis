package org.molgenis.ontology.sorta.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.DECIMAL;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.ontology.core.model.OntologyPackage.PACKAGE_ONTOLOGY;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MatchingTaskContentEntityMetaData extends SystemEntityMetaData
{
	public static final String SIMPLE_NAME = "MatchingTaskContent";
	public static final String MATCHING_TASK_CONTENT = PACKAGE_ONTOLOGY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public final static String IDENTIFIER = "identifier";
	public final static String INPUT_TERM = "inputTerm";
	public final static String MATCHED_TERM = "matchTerm";
	public final static String SCORE = "score";
	public final static String VALIDATED = "validated";

	private final OntologyPackage ontologyPackage;

	@Autowired
	MatchingTaskContentEntityMetaData(OntologyPackage ontologyPackage)
	{
		super(SIMPLE_NAME, PACKAGE_ONTOLOGY);
		this.ontologyPackage = requireNonNull(ontologyPackage);
	}

	@Override
	public void init()
	{
		setPackage(ontologyPackage);

		setAbstract(true);
		addAttribute(IDENTIFIER, ROLE_ID);
		addAttribute(MATCHED_TERM).setDescription("Matched ontology term").setNillable(true);
		addAttribute(SCORE).setDataType(DECIMAL).setDescription("Score of the match").setNillable(true);
		addAttribute(VALIDATED).setDataType(BOOL).setDescription("Indication if the match was validated")
				.setNillable(false);
	}
}
