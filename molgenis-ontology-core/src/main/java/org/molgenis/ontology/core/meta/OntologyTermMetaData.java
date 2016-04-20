package org.molgenis.ontology.core.meta;

import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LABEL;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.stereotype.Component;

@Component
public class OntologyTermMetaData extends EntityMetaData
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

		addAttribute(new AttributeMetaData(ID).setVisible(false), ROLE_ID);
		addAttribute(new AttributeMetaData(ONTOLOGY_TERM_IRI, FieldTypeEnum.STRING).setNillable(false));
		addAttribute(new AttributeMetaData(ONTOLOGY_TERM_NAME, FieldTypeEnum.TEXT).setNillable(false),
				ROLE_LABEL);
		addAttribute(new AttributeMetaData(ONTOLOGY_TERM_SYNONYM, FieldTypeEnum.MREF).setNillable(true)
				.setRefEntity(OntologyTermSynonymMetaData.INSTANCE));
		addAttribute(new AttributeMetaData(ONTOLOGY_TERM_DYNAMIC_ANNOTATION, FieldTypeEnum.MREF)
				.setNillable(true).setRefEntity(OntologyTermDynamicAnnotationMetaData.INSTANCE));
		addAttribute(new AttributeMetaData(ONTOLOGY_TERM_NODE_PATH, FieldTypeEnum.MREF).setNillable(true)
				.setRefEntity(OntologyTermNodePathMetaData.INSTANCE));
		addAttribute(new AttributeMetaData(ONTOLOGY, FieldTypeEnum.XREF).setNillable(false)
				.setRefEntity(OntologyMetaData.INSTANCE));
	}
}