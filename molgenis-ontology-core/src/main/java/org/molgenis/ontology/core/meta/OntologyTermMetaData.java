package org.molgenis.ontology.core.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LABEL;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.molgenis.data.meta.TagMetaData;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OntologyTermMetaData extends SystemEntityMetaDataImpl
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

	private OntologyTermSynonymMetaData ontologyTermSynonymMetaData;
	private OntologyTermDynamicAnnotationMetaData ontologyTermDynamicAnnotationMetaData;
	private OntologyTermNodePathMetaData ontologyTermNodePathMetaData;
	private OntologyMetaData ontologyMetaData;

	@Override
	public void init()
	{
		setName(SIMPLE_NAME);
		setPackage(OntologyPackage.getPackageInstance());

		addAttribute(ID, ROLE_ID).setVisible(false);
		addAttribute(ONTOLOGY_TERM_IRI).setNillable(false);
		addAttribute(ONTOLOGY_TERM_NAME, ROLE_LABEL).setDataType(TEXT).setNillable(false);
		addAttribute(ONTOLOGY_TERM_SYNONYM).setDataType(MREF).setNillable(true).setRefEntity(ontologyTermSynonymMetaData);
		addAttribute(ONTOLOGY_TERM_DYNAMIC_ANNOTATION).setDataType(MREF)
				.setNillable(true).setRefEntity(ontologyTermDynamicAnnotationMetaData);
		addAttribute(ONTOLOGY_TERM_NODE_PATH).setDataType(MREF).setNillable(true)
				.setRefEntity(ontologyTermNodePathMetaData);
		addAttribute(ONTOLOGY).setDataType(XREF).setNillable(false)
				.setRefEntity(ontologyMetaData);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setOntologyTermSynonymMetaData(OntologyTermSynonymMetaData ontologyTermSynonymMetaData) {
		this.ontologyTermSynonymMetaData = requireNonNull(ontologyTermSynonymMetaData);
	}

	@Autowired
	public void setOntologyTermDynamicAnnotationMetaData(OntologyTermDynamicAnnotationMetaData ontologyTermDynamicAnnotationMetaData) {
		this.ontologyTermDynamicAnnotationMetaData = requireNonNull(ontologyTermDynamicAnnotationMetaData);
	}

	@Autowired
	public void setOntologyTermNodePathMetaData(OntologyTermNodePathMetaData ontologyTermNodePathMetaData) {
		this.ontologyTermNodePathMetaData = requireNonNull(ontologyTermNodePathMetaData);
	}

	@Autowired
	public void setOntologyMetaData(OntologyMetaData ontologyMetaData) {
		this.ontologyMetaData = requireNonNull(ontologyMetaData);
	}
}