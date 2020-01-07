package org.molgenis.ontology.core.ic;

import static org.molgenis.ontology.core.ic.TermFrequencyMetaData.FREQUENCY;
import static org.molgenis.ontology.core.ic.TermFrequencyMetaData.OCCURRENCE;
import static org.molgenis.ontology.core.ic.TermFrequencyMetaData.TERM;
import static org.molgenis.ontology.core.ic.TermFrequencyMetaData.TERM_FREQUENCY;
import static org.molgenis.util.ApplicationContextProvider.getApplicationContext;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;

public class OntologyTermFrequencyServiceImpl implements TermFrequencyService {

  private final PubMedTermFrequencyService pubMedTermFrequencyService =
      new PubMedTermFrequencyService();
  private final DataService dataService;

  public OntologyTermFrequencyServiceImpl(DataService dataService) {
    if (dataService == null) {
      throw new IllegalArgumentException("dataService is null");
    }
    this.dataService = dataService;
  }

  @Override
  public Double getTermFrequency(String term) {
    Entity entity = getTermFrequencyEntity(term);
    if (entity == null) {
      return null;
    } else {
      return entity.getDouble(FREQUENCY);
    }
  }

  private Entity getTermFrequencyEntity(String term) {
    Entity entity = dataService.findOne(TERM_FREQUENCY, new QueryImpl<>().eq(TERM, term));
    if (entity == null) {
      entity = addEntry(term, pubMedTermFrequencyService.getTermFrequency(term), dataService);
    }
    return entity;
  }

  private Entity addEntry(String term, PubMedTFEntity pubMedTFEntity, DataService dataService) {
    if (pubMedTFEntity == null) {
      return null;
    }

    // FIXME remove reference to getApplicationContext
    TermFrequencyMetaData termFrequencyEntityType =
        getApplicationContext().getBean(TermFrequencyMetaData.class);
    Entity entity = new DynamicEntity(termFrequencyEntityType);
    entity.set(TERM, term);
    entity.set(FREQUENCY, pubMedTFEntity.getFrequency());
    entity.set(OCCURRENCE, pubMedTFEntity.getOccurrence());
    dataService.add(TERM_FREQUENCY, entity);
    return entity;
  }
}
