package org.molgenis.ontology.core.meta;

import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.ontology.core.model.OntologyPackage.PACKAGE_ONTOLOGY;

import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SemanticTypeMetaData extends SystemEntityMetaData
{

	public final static String SIMPLE_NAME = "SemanticType";
	public static final String SEMANTIC_TYPE = PACKAGE_ONTOLOGY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public final static String ID = "id";
	public final static String SEMANTIC_TYPE_NAME = "semanticTypeName";
	public final static String SEMANTIC_TYPE_GROUP = "semanticTypeGroup";
	public final static String SEMANTIC_TYPE_GLOBAL_KEY_CONCEPT = "globalKeyConcept";

	private final OntologyPackage ontologyPackage;

	@Autowired
	SemanticTypeMetaData(OntologyPackage ontologyPackage)
	{
		super(SIMPLE_NAME, PACKAGE_ONTOLOGY);
		this.ontologyPackage = ontologyPackage;
	}

	@Override
	protected void init()
	{
		setLabel("Ontology term semantic type");
		setPackage(ontologyPackage);

		addAttribute(ID, AttributeRole.ROLE_ID);
		addAttribute(SEMANTIC_TYPE_NAME, AttributeRole.ROLE_LABEL);
		addAttribute(SEMANTIC_TYPE_GROUP);
		addAttribute(SEMANTIC_TYPE_GLOBAL_KEY_CONCEPT).setDataType(AttributeType.BOOL);

	}
}