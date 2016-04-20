package org.molgenis.ontology.core.meta;

import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LABEL;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.stereotype.Component;

@Component
public class OntologyMetaData extends EntityMetaData
{
	public final static String ID = "id";
	public final static String ONTOLOGY_IRI = "ontologyIRI";
	public final static String ONTOLOGY_NAME = "ontologyName";
	public final static String SIMPLE_NAME = "Ontology";
	public final static String ENTITY_NAME = OntologyPackage.PACKAGE_NAME + "_" + SIMPLE_NAME;

	public final static OntologyMetaData INSTANCE = new OntologyMetaData();

	private OntologyMetaData()
	{
		super(SIMPLE_NAME, OntologyPackage.getPackageInstance());
		addAttribute(new AttributeMetaData(ID).setVisible(false), ROLE_ID);
		addAttribute(new AttributeMetaData(ONTOLOGY_IRI, FieldTypeEnum.STRING).setNillable(false));
		addAttribute(new AttributeMetaData(ONTOLOGY_NAME, FieldTypeEnum.STRING).setNillable(false),
				ROLE_LABEL);
	}
}