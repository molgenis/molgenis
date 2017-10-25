package org.molgenis.ontology.core.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.ontology.core.meta.*;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.molgenis.ontology.core.repository.OntologyRepository;
import org.molgenis.ontology.core.repository.OntologyTermRepository;
import org.molgenis.ontology.core.service.impl.OntologyServiceImpl;
import org.molgenis.ontology.importer.OntologyImportService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, OntologyMetaData.class, OntologyFactory.class, OntologyTermMetaData.class,
		OntologyTermFactory.class, OntologyTermDynamicAnnotationMetaData.class,
		OntologyTermDynamicAnnotationFactory.class, OntologyTermNodePathMetaData.class,
		OntologyTermNodePathFactory.class, OntologyTermSynonymMetaData.class, OntologyTermSynonymFactory.class,
		OntologyPackage.class, OntologyServiceImpl.class, OntologyRepository.class, OntologyTermRepository.class,
		OntologyImportService.class, OntologyFactory.class })
public class OntologyTestConfig
{

}
