package org.molgenis.ontology.model;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public class OntologyTermMetaData extends DefaultEntityMetaData
{
	public final static String ID = "id";
	public final static String ONTOLOGY_TERM_IRI = "ontologyTermIRI";
	public final static String ONTOLOGY_TERM_NAME = "ontologyTermName";
	public final static String ONTOLOGY_TERM_SYNONYM = "ontologyTermSynonym";
	public final static String ONTOLOGY_TERM_DYNAMIC_ANNOTATION = "ontologyTermDynamicAnnotation";
	public final static String ONTOLOGY_TERM_NODE_PATH = "nodePath";
	public final static String ONTOLOGY = "ontology";
	public final static String SIMPLE_NAME = "OntologyTerm";
	public final static String ENTITY_NAME = OntologyPackage.PACKAGE_NAME + "_" + SIMPLE_NAME;
	public final static OntologyTermMetaData INSTANCE = new OntologyTermMetaData();

	private OntologyTermMetaData()
	{
		super(SIMPLE_NAME, OntologyPackage.getPackageInstance());

		addAttributeMetaData(new DefaultAttributeMetaData(ID).setIdAttribute(true).setNillable(false).setVisible(false));
		addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_TERM_IRI, FieldTypeEnum.STRING).setNillable(false));
		addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_TERM_NAME, FieldTypeEnum.STRING).setNillable(false)
				.setLabelAttribute(true));
		addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_TERM_SYNONYM, FieldTypeEnum.MREF).setNillable(true)
				.setRefEntity(OntologyTermSynonymMetaData.INSTANCE));
		addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_TERM_DYNAMIC_ANNOTATION, FieldTypeEnum.MREF)
				.setNillable(true).setRefEntity(OntologyTermDynamicAnnotationMetaData.INSTANCE));
		addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_TERM_NODE_PATH, FieldTypeEnum.MREF)
				.setNillable(true).setRefEntity(OntologyTermNodePathMetaData.INSTANCE));
		addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY, FieldTypeEnum.XREF).setNillable(false)
				.setRefEntity(OntologyMetaData.INSTANCE));
	}
}