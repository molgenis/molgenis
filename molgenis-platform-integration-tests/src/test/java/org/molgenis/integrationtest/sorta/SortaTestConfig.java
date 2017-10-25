package org.molgenis.integrationtest.sorta;

import org.molgenis.ontology.SortaConfig;
import org.molgenis.ontology.roc.InformationContentService;
import org.molgenis.ontology.sorta.controller.SortaController;
import org.molgenis.ontology.sorta.job.SortaJobExecutionFactory;
import org.molgenis.ontology.sorta.job.SortaJobFactory;
import org.molgenis.ontology.sorta.meta.MatchingTaskContentMetaData;
import org.molgenis.ontology.sorta.meta.OntologyTermHitMetaData;
import org.molgenis.ontology.sorta.meta.SortaJobExecutionMetaData;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ SortaConfig.class, SortaController.class, InformationContentService.class, OntologyTermHitMetaData.class,
		SortaJobFactory.class, MatchingTaskContentMetaData.class, SortaJobExecutionMetaData.class,
		SortaJobExecutionFactory.class })
public class SortaTestConfig
{
}
