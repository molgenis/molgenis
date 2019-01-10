package org.molgenis.ontology.sorta.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.DECIMAL;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.ontology.core.meta.OntologyTermMetadata.ONTOLOGY;
import static org.molgenis.ontology.core.meta.OntologyTermMetadata.ONTOLOGY_TERM_DYNAMIC_ANNOTATION;
import static org.molgenis.ontology.core.meta.OntologyTermMetadata.ONTOLOGY_TERM_IRI;
import static org.molgenis.ontology.core.meta.OntologyTermMetadata.ONTOLOGY_TERM_NAME;
import static org.molgenis.ontology.core.meta.OntologyTermMetadata.ONTOLOGY_TERM_NODE_PATH;
import static org.molgenis.ontology.core.meta.OntologyTermMetadata.ONTOLOGY_TERM_SYNONYM;
import static org.molgenis.ontology.core.model.OntologyPackage.PACKAGE_ONTOLOGY;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.ontology.core.meta.OntologyMetadata;
import org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotationMetadata;
import org.molgenis.ontology.core.meta.OntologyTermNodePathMetadata;
import org.molgenis.ontology.core.meta.OntologyTermSynonymMetadata;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.stereotype.Component;

@Component
public class OntologyTermHitMetaData extends SystemEntityType {
  private static final String SIMPLE_NAME = "OntologyTermHit";
  public static final String ONTOLOGY_TERM_HIT = PACKAGE_ONTOLOGY + PACKAGE_SEPARATOR + SIMPLE_NAME;

  public static final String ID = "id";
  public static final String SCORE = "Score";
  public static final String COMBINED_SCORE = "Combined_Score";

  private final OntologyPackage ontologyPackage;
  private final OntologyTermSynonymMetadata ontologyTermSynonymMetadata;
  private final OntologyTermDynamicAnnotationMetadata ontologyTermDynamicAnnotationMetadata;
  private final OntologyTermNodePathMetadata ontologyTermNodePathMetadata;
  private final OntologyMetadata ontologyMetadata;

  public OntologyTermHitMetaData(
      OntologyPackage ontologyPackage,
      OntologyTermSynonymMetadata ontologyTermSynonymMetadata,
      OntologyTermDynamicAnnotationMetadata ontologyTermDynamicAnnotationMetadata,
      OntologyTermNodePathMetadata ontologyTermNodePathMetadata,
      OntologyMetadata ontologyMetadata) {
    super(SIMPLE_NAME, PACKAGE_ONTOLOGY);
    this.ontologyPackage = requireNonNull(ontologyPackage);
    this.ontologyTermSynonymMetadata = requireNonNull(ontologyTermSynonymMetadata);
    this.ontologyTermDynamicAnnotationMetadata =
        requireNonNull(ontologyTermDynamicAnnotationMetadata);
    this.ontologyTermNodePathMetadata = requireNonNull(ontologyTermNodePathMetadata);
    this.ontologyMetadata = requireNonNull(ontologyMetadata);
  }

  @Override
  public void init() {
    setLabel("Ontology term hit");
    setPackage(ontologyPackage);

    addAttribute(ID, ROLE_ID).setAuto(true);
    addAttribute(SCORE).setDataType(DECIMAL);
    addAttribute(COMBINED_SCORE).setDataType(DECIMAL);

    // Append OntologyTermMetaData attributes with the same name (required by SORTA)
    addAttribute(ONTOLOGY_TERM_IRI).setNillable(false);
    addAttribute(ONTOLOGY_TERM_NAME, ROLE_LABEL).setDataType(TEXT).setNillable(false);
    addAttribute(ONTOLOGY_TERM_SYNONYM)
        .setDataType(MREF)
        .setNillable(true)
        .setRefEntity(ontologyTermSynonymMetadata);
    addAttribute(ONTOLOGY_TERM_DYNAMIC_ANNOTATION)
        .setDataType(MREF)
        .setNillable(true)
        .setRefEntity(ontologyTermDynamicAnnotationMetadata);
    addAttribute(ONTOLOGY_TERM_NODE_PATH)
        .setDataType(MREF)
        .setNillable(true)
        .setRefEntity(ontologyTermNodePathMetadata);
    addAttribute(ONTOLOGY).setDataType(XREF).setNillable(false).setRefEntity(ontologyMetadata);
  }
}
