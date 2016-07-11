package org.molgenis.ontology.core.meta;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.DECIMAL;
import static org.molgenis.MolgenisFieldTypes.AttributeType.TEXT;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.ontology.core.model.OntologyPackage.PACKAGE_ONTOLOGY;

@Component
public class OntologyTermSynonymMetaData extends SystemEntityMetaData
{
	public static final String SIMPLE_NAME = "OntologyTermSynonym";
	public final static String ONTOLOGY_TERM_SYNONYM = PACKAGE_ONTOLOGY + "_" + SIMPLE_NAME;

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