package org.molgenis.ontology.model;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public class OntologyMetaData
{
	public final static String ID = "id";
	public final static String ONTOLOGY_IRI = "ontologyIRI";
	public final static String ONTOLOGY_NAME = "ontologyName";
	public final static String ENTITY_NAME = "Ontology";

	public static EntityMetaData getEntityMetaData()
	{
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData(ENTITY_NAME);
		DefaultAttributeMetaData idAttr = new DefaultAttributeMetaData(ID);
		idAttr.setIdAttribute(true);
		idAttr.setNillable(false);
		idAttr.setVisible(false);
		entityMetaData.addAttributeMetaData(idAttr);

		DefaultAttributeMetaData ontologyIRIAttr = new DefaultAttributeMetaData(ONTOLOGY_IRI, FieldTypeEnum.STRING);
		ontologyIRIAttr.setNillable(false);
		entityMetaData.addAttributeMetaData(ontologyIRIAttr);

		DefaultAttributeMetaData ontologyNameAttr = new DefaultAttributeMetaData(ONTOLOGY_NAME, FieldTypeEnum.STRING);
		ontologyNameAttr.setNillable(false);
		ontologyNameAttr.setLabelAttribute(true);
		entityMetaData.addAttributeMetaData(ontologyNameAttr);

		return entityMetaData;
	}
}