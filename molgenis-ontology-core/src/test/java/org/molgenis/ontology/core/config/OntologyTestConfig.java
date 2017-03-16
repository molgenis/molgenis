package org.molgenis.ontology.core.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.ontology.core.meta.*;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, OntologyMetaData.class, OntologyFactory.class, OntologyTermMetaData.class,
		OntologyTermFactory.class, OntologyTermDynamicAnnotationMetaData.class,
		OntologyTermDynamicAnnotationFactory.class, OntologyTermNodePathMetaData.class,
		OntologyTermNodePathFactory.class, OntologyTermSynonymMetaData.class, OntologyTermSynonymFactory.class,
		OntologyPackage.class })
public class OntologyTestConfig
{
}
