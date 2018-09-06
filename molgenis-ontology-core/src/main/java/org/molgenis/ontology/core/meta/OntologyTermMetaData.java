package org.molgenis.ontology.core.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.ontology.core.model.OntologyPackage.PACKAGE_ONTOLOGY;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.stereotype.Component;

@Component
public class OntologyTermMetaData extends SystemEntityType {
  public static final String SIMPLE_NAME = "OntologyTerm";
  public static final String ONTOLOGY_TERM = PACKAGE_ONTOLOGY + PACKAGE_SEPARATOR + SIMPLE_NAME;

  public static final String ID = "id";
  public static final String ONTOLOGY_TERM_IRI = "ontologyTermIRI";
  public static final String ONTOLOGY_TERM_NAME = "ontologyTermName";
  public static final String ONTOLOGY_TERM_SYNONYM = "ontologyTermSynonym";
  public static final String ONTOLOGY_TERM_DYNAMIC_ANNOTATION = "ontologyTermDynamicAnnotation";
  public static final String ONTOLOGY_TERM_NODE_PATH = "nodePath";
  public static final String ONTOLOGY = "ontology";

  private final OntologyPackage ontologyPackage;
  private final OntologyTermSynonymMetaData ontologyTermSynonymMetaData;
  private final OntologyTermDynamicAnnotationMetaData ontologyTermDynamicAnnotationMetaData;
  private final OntologyTermNodePathMetaData ontologyTermNodePathMetaData;
  private final OntologyMetaData ontologyMetaData;

  public OntologyTermMetaData(
      OntologyPackage ontologyPackage,
      OntologyTermSynonymMetaData ontologyTermSynonymMetaData,
      OntologyTermDynamicAnnotationMetaData ontologyTermDynamicAnnotationMetaData,
      OntologyTermNodePathMetaData ontologyTermNodePathMetaData,
      OntologyMetaData ontologyMetaData) {
    super(SIMPLE_NAME, PACKAGE_ONTOLOGY);
    this.ontologyPackage = requireNonNull(ontologyPackage);
    this.ontologyTermSynonymMetaData = requireNonNull(ontologyTermSynonymMetaData);
    this.ontologyTermDynamicAnnotationMetaData =
        requireNonNull(ontologyTermDynamicAnnotationMetaData);
    this.ontologyTermNodePathMetaData = requireNonNull(ontologyTermNodePathMetaData);
    this.ontologyMetaData = requireNonNull(ontologyMetaData);
  }

  @Override
  public void init() {
    setLabel("Ontology term");
    setPackage(ontologyPackage);

    addAttribute(ID, ROLE_ID).setVisible(false);
    addAttribute(ONTOLOGY_TERM_IRI).setNillable(false);
    addAttribute(ONTOLOGY_TERM_NAME, ROLE_LABEL).setDataType(TEXT).setNillable(false);
    addAttribute(ONTOLOGY_TERM_SYNONYM)
        .setDataType(MREF)
        .setNillable(true)
        .setRefEntity(ontologyTermSynonymMetaData);
    addAttribute(ONTOLOGY_TERM_DYNAMIC_ANNOTATION)
        .setDataType(MREF)
        .setNillable(true)
        .setRefEntity(ontologyTermDynamicAnnotationMetaData);
    addAttribute(ONTOLOGY_TERM_NODE_PATH)
        .setDataType(MREF)
        .setNillable(true)
        .setRefEntity(ontologyTermNodePathMetaData);
    addAttribute(ONTOLOGY).setDataType(XREF).setNillable(false).setRefEntity(ontologyMetaData);
  }
}
