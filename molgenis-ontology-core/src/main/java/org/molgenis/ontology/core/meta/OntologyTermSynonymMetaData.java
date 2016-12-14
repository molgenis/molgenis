package org.molgenis.ontology.core.meta;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.DECIMAL;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.ontology.core.model.OntologyPackage.PACKAGE_ONTOLOGY;

@Component
public class OntologyTermSynonymMetaData extends SystemEntityType
{
	public static final String SIMPLE_NAME = "OntologyTermSynonym";
	public final static String ONTOLOGY_TERM_SYNONYM = PACKAGE_ONTOLOGY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public final static String ID = "id";
	public final static String ONTOLOGY_TERM_SYNONYM_ATTR = "ontologyTermSynonym";
	public static final String SCORE = "Score";
	public static final String COMBINED_SCORE = "Combined_Score";

	private OntologyPackage ontologyPackage;

	public OntologyTermSynonymMetaData()
	{
		super(SIMPLE_NAME, PACKAGE_ONTOLOGY);
	}

	@Override
	public void init()
	{
		setLabel("Ontology term synonym");
		setPackage(ontologyPackage);

		addAttribute(ID, ROLE_ID).setVisible(false);
		addAttribute(ONTOLOGY_TERM_SYNONYM_ATTR, ROLE_LABEL).setDataType(TEXT).setNillable(false);
		addAttribute(SCORE).setDataType(DECIMAL);
		addAttribute(COMBINED_SCORE).setDataType(DECIMAL);
	}

	@Autowired
	public void setOntologyPackage(OntologyPackage ontologyPackage)
	{
		this.ontologyPackage = requireNonNull(ontologyPackage);
	}
}