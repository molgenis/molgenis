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
public class OntologyMetadata extends SystemEntityType {
  public static final String SIMPLE_NAME = "Ontology";
  public static final String ONTOLOGY = PACKAGE_ONTOLOGY + PACKAGE_SEPARATOR + SIMPLE_NAME;

  public static final String ID = "id";
  public static final String ONTOLOGY_IRI = "ontologyIRI";
  public static final String ONTOLOGY_NAME = "ontologyName";

  private final OntologyPackage ontologyPackage;

  public OntologyMetadata(OntologyPackage ontologyPackage) {
    super(SIMPLE_NAME, PACKAGE_ONTOLOGY);
    this.ontologyPackage = requireNonNull(ontologyPackage);
  }

  @Override
  public void init() {
    setLabel(SIMPLE_NAME);
    setPackage(ontologyPackage);

    addAttribute(ID, ROLE_ID).setVisible(false);
    addAttribute(ONTOLOGY_IRI)
        .setNillable(false)
        .setLabel("IRI")
        .setDescription("IRI which is used to identify an ontology");
    addAttribute(ONTOLOGY_NAME, ROLE_LABEL).setNillable(false).setLabel("Name");
  }
}
