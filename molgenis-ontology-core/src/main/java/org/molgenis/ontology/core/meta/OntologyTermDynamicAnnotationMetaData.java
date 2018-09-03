package org.molgenis.ontology.core.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.ontology.core.model.OntologyPackage.PACKAGE_ONTOLOGY;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.stereotype.Component;

@Component
public class OntologyTermDynamicAnnotationMetaData extends SystemEntityType {
  public static final String SIMPLE_NAME = "OntologyTermDynamicAnnotation";
  public static final String ONTOLOGY_TERM_DYNAMIC_ANNOTATION =
      PACKAGE_ONTOLOGY + PACKAGE_SEPARATOR + SIMPLE_NAME;

  public static final String ID = "id";
  public static final String NAME = "name";
  public static final String VALUE = "value";
  public static final String LABEL = "label";

  private final OntologyPackage ontologyPackage;

  public OntologyTermDynamicAnnotationMetaData(OntologyPackage ontologyPackage) {
    super(SIMPLE_NAME, PACKAGE_ONTOLOGY);
    this.ontologyPackage = requireNonNull(ontologyPackage);
  }

  @Override
  public void init() {
    setLabel("Ontology term dynamic annotation");
    setPackage(ontologyPackage);

    addAttribute(ID, ROLE_ID).setVisible(false);
    addAttribute(NAME).setNillable(false);
    addAttribute(VALUE).setNillable(false);
    addAttribute(LABEL, ROLE_LABEL).setNillable(false);
  }
}
