package org.molgenis.ontology.ic;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.meta.OntologyTermSynonymMetaData;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

public class OntologyTermFrequencyServiceImpl implements TermFrequencyService
{
	private final static int BATCH_SIZE = 10000;
	private final PubMedTermFrequencyService pubMedTermFrequencyService = new PubMedTermFrequencyService();
	private final DataService dataService;

	@Autowired
	public OntologyTermFrequencyServiceImpl(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		this.dataService = dataService;
	}

	public Double getTermFrequency(String term)
	{
		Entity entity = dataService.findOne(TermFrequencyEntityMetaData.ENTITY_NAME,
				new QueryImpl().eq(TermFrequencyEntityMetaData.TERM, term));

		if (entity != null && entity.getInt(TermFrequencyEntityMetaData.FREQUENCY) != 0)
		{
			return entity.getDouble(TermFrequencyEntityMetaData.FREQUENCY);
		}
		else
		{
			return addEntry(term, pubMedTermFrequencyService.getTermFrequency(term), dataService);
		}
	}

	private Double addEntry(String term, PubMedTFEntity pubMedTFEntity, DataService dataService)
	{
		if (pubMedTFEntity == null) return null;

		MapEntity mapEntity = new MapEntity();
		mapEntity.set(TermFrequencyEntityMetaData.TERM, term);
		mapEntity.set(TermFrequencyEntityMetaData.FREQUENCY, pubMedTFEntity.getFrequency());
		mapEntity.set(TermFrequencyEntityMetaData.OCCURRENCE, pubMedTFEntity.getOccurrence());
		dataService.add(TermFrequencyEntityMetaData.ENTITY_NAME, mapEntity);
		return pubMedTFEntity.getFrequency();
	}

	@Async
	@RunAsSystem
	public void updateTermFrequency()
	{
		// Remove all the existing term frequency records
		dataService.deleteAll(TermFrequencyEntityMetaData.ENTITY_NAME);

		List<Entity> entitiesToAdd = new ArrayList<Entity>();
		// Create new term frequency records
		for (Entity entity : dataService.findAll(OntologyTermSynonymMetaData.ENTITY_NAME))
		{
			String ontologyTermSynonym = entity.getString(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM);
			PubMedTFEntity pubMedTFEntity = pubMedTermFrequencyService.getTermFrequency(ontologyTermSynonym);

			if (pubMedTFEntity != null)
			{
				MapEntity mapEntity = new MapEntity();
				mapEntity.set(TermFrequencyEntityMetaData.TERM, ontologyTermSynonym);
				mapEntity.set(TermFrequencyEntityMetaData.FREQUENCY, pubMedTFEntity.getFrequency());
				mapEntity.set(TermFrequencyEntityMetaData.OCCURRENCE, pubMedTFEntity.getOccurrence());
				entitiesToAdd.add(mapEntity);

				if (entitiesToAdd.size() > BATCH_SIZE)
				{
					dataService.add(TermFrequencyEntityMetaData.ENTITY_NAME, entitiesToAdd);
					entitiesToAdd.clear();
				}
			}
		}

		if (entitiesToAdd.size() != 0)
		{
			dataService.add(TermFrequencyEntityMetaData.ENTITY_NAME, entitiesToAdd);
		}
	}
}
