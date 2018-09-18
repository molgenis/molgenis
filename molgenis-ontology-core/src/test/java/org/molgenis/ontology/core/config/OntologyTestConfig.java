package org.molgenis.ontology.core.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.ontology.core.meta.OntologyFactory;
import org.molgenis.ontology.core.meta.OntologyMetaData;
import org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotationFactory;
import org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotationMetaData;
import org.molgenis.ontology.core.meta.OntologyTermFactory;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.core.meta.OntologyTermNodePathFactory;
import org.molgenis.ontology.core.meta.OntologyTermNodePathMetaData;
import org.molgenis.ontology.core.meta.OntologyTermSynonymFactory;
import org.molgenis.ontology.core.meta.OntologyTermSynonymMetaData;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  EntityBaseTestConfig.class,
  OntologyMetaData.class,
  OntologyFactory.class,
  OntologyTermMetaData.class,
  OntologyTermFactory.class,
  OntologyTermDynamicAnnotationMetaData.class,
  OntologyTermDynamicAnnotationFactory.class,
  OntologyTermNodePathMetaData.class,
  OntologyTermNodePathFactory.class,
  OntologyTermSynonymMetaData.class,
  OntologyTermSynonymFactory.class,
  OntologyPackage.class
})
public class OntologyTestConfig {}
