package org.molgenis.ontology.core.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;
import static org.molgenis.ontology.core.model.OntologyPackage.PACKAGE_ONTOLOGY;

import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OntologyTermMetaData extends SystemEntityMetaDataImpl
{
	public final static String SIMPLE_NAME = "OntologyTerm";
	public final static String ONTOLOGY_TERM = PACKAGE_ONTOLOGY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public final static String ID = "id";
	public final static String ONTOLOGY_TERM_IRI = "ontologyTermIRI";
	public final static String ONTOLOGY_TERM_NAME = "ontologyTermName";
	public final static String ONTOLOGY_TERM_SYNONYM = "ontologyTermSynonym";
	public final static String ONTOLOGY_TERM_DYNAMIC_ANNOTATION = "ontologyTermDynamicAnnotation";
	public final static String ONTOLOGY_TERM_NODE_PATH = "nodePath";
	public final static String ONTOLOGY = "ontology";

	private OntologyTermSynonymMetaData ontologyTermSynonymMetaData;
	private OntologyTermDynamicAnnotationMetaData ontologyTermDynamicAnnotationMetaData;
	private OntologyTermNodePathMetaData ontologyTermNodePathMetaData;
	private OntologyMetaData ontologyMetaData;

	private OntologyPackage ontologyPackage;

	public OntologyTermMetaData()
	{
		super(SIMPLE_NAME, PACKAGE_ONTOLOGY);
	}

	@Override
	public void init()
	{
		setPackage(ontologyPackage);

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

	@Autowired
	public void setOntologyPackage(OntologyPackage ontologyPackage)
	{
		this.ontologyPackage = requireNonNull(ontologyPackage);
	}
}