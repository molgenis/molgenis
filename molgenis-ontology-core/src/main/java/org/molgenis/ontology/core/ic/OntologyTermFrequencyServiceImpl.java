package org.molgenis.ontology.core.ic;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.meta.OntologyTermSynonymMetaData;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.scheduling.annotation.Async;

import java.util.ArrayList;
import java.util.List;

import static org.molgenis.ontology.core.ic.TermFrequencyMetaData.*;
import static org.molgenis.ontology.core.meta.OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM;
import static org.molgenis.util.ApplicationContextProvider.getApplicationContext;

public class OntologyTermFrequencyServiceImpl implements TermFrequencyService
{
	private final static int BATCH_SIZE = 10000;
	private final PubMedTermFrequencyService pubMedTermFrequencyService = new PubMedTermFrequencyService();
	private final DataService dataService;

	public OntologyTermFrequencyServiceImpl(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		this.dataService = dataService;
	}

	@Override
	public Double getTermFrequency(String term)
	{
		Entity entity = getTermFrequencyEntity(term);
		if (entity == null) return null;
		else return entity.getDouble(FREQUENCY);
	}

	@Override
	public Integer getTermOccurrence(String term)
	{
		Entity entity = getTermFrequencyEntity(term);
		if (entity == null) return null;
		else return entity.getInt(OCCURRENCE);
	}

	private Entity getTermFrequencyEntity(String term)
	{
		Entity entity = dataService.findOne(TERM_FREQUENCY, new QueryImpl<>().eq(TERM, term));
		if (entity == null)
		{
			entity = addEntry(term, pubMedTermFrequencyService.getTermFrequency(term), dataService);
		}
		return entity;
	}

	private Entity addEntry(String term, PubMedTFEntity pubMedTFEntity, DataService dataService)
	{
		if (pubMedTFEntity == null) return null;

		// FIXME remove reference to getApplicationContext
		TermFrequencyMetaData termFrequencyEntityType = getApplicationContext().getBean(TermFrequencyMetaData.class);
		Entity entity = new DynamicEntity(termFrequencyEntityType);
		entity.set(TERM, term);
		entity.set(FREQUENCY, pubMedTFEntity.getFrequency());
		entity.set(OCCURRENCE, pubMedTFEntity.getOccurrence());
		dataService.add(TERM_FREQUENCY, entity);
		return entity;
	}

	@Override
	@Async
	@RunAsSystem
	public void updateTermFrequency()
	{
		// Remove all the existing term frequency records
		dataService.deleteAll(TERM_FREQUENCY);

		List<Entity> entitiesToAdd = new ArrayList<>();
		// Create new term frequency records
		dataService.findAll(ONTOLOGY_TERM_SYNONYM).forEach(entity ->
		{
			String ontologyTermSynonym = entity.getString(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR);
			PubMedTFEntity pubMedTFEntity = pubMedTermFrequencyService.getTermFrequency(ontologyTermSynonym);

			if (pubMedTFEntity != null)
			{
				// FIXME remove reference to getApplicationContext
				TermFrequencyMetaData termFrequencyEntityType = getApplicationContext().getBean(
						TermFrequencyMetaData.class);
				Entity mapEntity = new DynamicEntity(termFrequencyEntityType);
				mapEntity.set(TERM, ontologyTermSynonym);
				mapEntity.set(FREQUENCY, pubMedTFEntity.getFrequency());
				mapEntity.set(OCCURRENCE, pubMedTFEntity.getOccurrence());
				entitiesToAdd.add(mapEntity);

				if (entitiesToAdd.size() > BATCH_SIZE)
				{
					dataService.add(TERM_FREQUENCY, entitiesToAdd.stream());
					entitiesToAdd.clear();
				}
			}
		});

		if (!entitiesToAdd.isEmpty())
		{
			dataService.add(TERM_FREQUENCY, entitiesToAdd.stream());
		}
	}
}
