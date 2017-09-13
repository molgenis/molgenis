package org.molgenis.ontology.sorta.meta;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.DECIMAL;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.ontology.core.model.OntologyPackage.PACKAGE_ONTOLOGY;

@Component
public class OntologyTermHitMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "OntologyTermHit";
	public static final String ONTOLOGY_TERM_HIT = PACKAGE_ONTOLOGY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String SCORE = "Score";
	public static final String COMBINED_SCORE = "Combined_Score";

	private final OntologyPackage ontologyPackage;

	public OntologyTermHitMetaData(OntologyPackage ontologyPackage)
	{
		super(SIMPLE_NAME, PACKAGE_ONTOLOGY);
		this.ontologyPackage = requireNonNull(ontologyPackage);
	}

	@Override
	public void init()
	{
		setLabel("Ontology term hit");
		setPackage(ontologyPackage);

		addAttribute(ID, ROLE_ID).setAuto(true);
		addAttribute(SCORE).setDataType(DECIMAL);
		addAttribute(COMBINED_SCORE).setDataType(DECIMAL);
	}
}
