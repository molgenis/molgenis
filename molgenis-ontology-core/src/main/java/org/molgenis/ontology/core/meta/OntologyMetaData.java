package org.molgenis.ontology.core.meta;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LABEL;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.stereotype.Component;

@Component
public class OntologyMetaData extends DefaultEntityMetaData
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
		addAttributeMetaData(new DefaultAttributeMetaData(ID).setVisible(false), ROLE_ID);
		addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_IRI, FieldTypeEnum.STRING).setNillable(false));
		addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_NAME, FieldTypeEnum.STRING).setNillable(false),
				ROLE_LABEL);
	}
}