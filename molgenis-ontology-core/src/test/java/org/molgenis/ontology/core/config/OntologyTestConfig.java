package org.molgenis.ontology.core.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.ontology.core.meta.OntologyFactory;
import org.molgenis.ontology.core.meta.OntologyMetadata;
import org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotationFactory;
import org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotationMetadata;
import org.molgenis.ontology.core.meta.OntologyTermFactory;
import org.molgenis.ontology.core.meta.OntologyTermMetadata;
import org.molgenis.ontology.core.meta.OntologyTermNodePathFactory;
import org.molgenis.ontology.core.meta.OntologyTermNodePathMetadata;
import org.molgenis.ontology.core.meta.OntologyTermSynonymFactory;
import org.molgenis.ontology.core.meta.OntologyTermSynonymMetadata;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  EntityBaseTestConfig.class,
  OntologyMetadata.class,
  OntologyFactory.class,
  OntologyTermMetadata.class,
  OntologyTermFactory.class,
  OntologyTermDynamicAnnotationMetadata.class,
  OntologyTermDynamicAnnotationFactory.class,
  OntologyTermNodePathMetadata.class,
  OntologyTermNodePathFactory.class,
  OntologyTermSynonymMetadata.class,
  OntologyTermSynonymFactory.class,
  OntologyPackage.class
})
public class OntologyTestConfig {}
