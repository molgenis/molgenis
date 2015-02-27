package org.molgenis.ontology.model;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public class OntologyTermMetaData
{
	public final static String ID = "id";
	public final static String ONTOLOGY_TERM_IRI = "ontologyTermIRI";
	public final static String ONTOLOGY_TERM_NAME = "ontologyTermName";
	public final static String ONTOLOGY_TERM_SYNONYM = "ontologyTermSynonym";
	public final static String ONTOLOGY_TERM_DYNAMIC_ANNOTATION = "ontologyTermDynamicAnnotation";
	public final static String ONTOLOGY = "ontology";

	public final static String ENTITY_NAME = "OntologyTerm";

	public static EntityMetaData getEntityMetaData()
	{
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData(ENTITY_NAME);
		DefaultAttributeMetaData idAttr = new DefaultAttributeMetaData(ID);
		idAttr.setIdAttribute(true);
		idAttr.setNillable(false);
		entityMetaData.addAttributeMetaData(idAttr);

		DefaultAttributeMetaData ontologyIRIAttr = new DefaultAttributeMetaData(ONTOLOGY_TERM_IRI, FieldTypeEnum.STRING);
		ontologyIRIAttr.setNillable(false);
		entityMetaData.addAttributeMetaData(ontologyIRIAttr);

		DefaultAttributeMetaData ontologyNameAttr = new DefaultAttributeMetaData(ONTOLOGY_TERM_NAME,
				FieldTypeEnum.STRING);
		ontologyNameAttr.setNillable(false);
		ontologyNameAttr.setLabelAttribute(true);
		entityMetaData.addAttributeMetaData(ontologyNameAttr);

		DefaultAttributeMetaData ontologyTermSynonymAttr = new DefaultAttributeMetaData(ONTOLOGY_TERM_SYNONYM,
				FieldTypeEnum.MREF);
		ontologyTermSynonymAttr.setNillable(true);
		ontologyTermSynonymAttr.setRefEntity(OntologyTermSynonymMetaData.getEntityMetaData());
		entityMetaData.addAttributeMetaData(ontologyTermSynonymAttr);

		DefaultAttributeMetaData ontologyTermDynamicAnnotationAttr = new DefaultAttributeMetaData(
				ONTOLOGY_TERM_DYNAMIC_ANNOTATION, FieldTypeEnum.MREF);
		ontologyTermDynamicAnnotationAttr.setNillable(true);
		ontologyTermDynamicAnnotationAttr.setRefEntity(OntologyTermDynamicAnnotationMetaData.getEntityMetaData());

		entityMetaData.addAttributeMetaData(ontologyTermDynamicAnnotationAttr);

		return entityMetaData;
	}
}