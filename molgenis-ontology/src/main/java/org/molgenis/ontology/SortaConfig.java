package org.molgenis.ontology;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.DataService;
import org.molgenis.ontology.core.ic.OntologyTermFrequencyServiceImpl;
import org.molgenis.ontology.core.ic.TermFrequencyService;
import org.molgenis.ontology.core.meta.OntologyTermSynonymFactory;
import org.molgenis.ontology.roc.InformationContentService;
import org.molgenis.ontology.sorta.meta.OntologyTermHitMetaData;
import org.molgenis.ontology.sorta.service.SortaService;
import org.molgenis.ontology.sorta.service.impl.SortaServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SortaConfig {
  private final DataService dataService;
  private final OntologyTermHitMetaData ontologyTermHitMetaData;
  private final OntologyTermSynonymFactory ontologyTermSynonymFactory;

  public SortaConfig(
      DataService dataService,
      OntologyTermHitMetaData ontologyTermHitMetaData,
      OntologyTermSynonymFactory ontologyTermSynonymFactory) {
    System.setProperty("jdk.xml.entityExpansionLimit", "1280000");
    this.dataService = requireNonNull(dataService);
    this.ontologyTermHitMetaData = requireNonNull(ontologyTermHitMetaData);
    this.ontologyTermSynonymFactory = requireNonNull(ontologyTermSynonymFactory);
  }

  @Bean
  public TermFrequencyService termFrequencyService() {
    return new OntologyTermFrequencyServiceImpl(dataService);
  }

  @Bean
  public SortaService sortaService() {
    return new SortaServiceImpl(
        dataService,
        informationContentService(),
        ontologyTermHitMetaData,
        ontologyTermSynonymFactory);
  }

  @Bean
  public InformationContentService informationContentService() {
    return new InformationContentService(dataService);
  }
}
