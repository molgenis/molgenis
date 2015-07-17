package org.molgenis.ontology.ic;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
		String termFrequency = getAttributeValue(term, TermFrequencyEntityMetaData.FREQUENCY);
		return StringUtils.isNotEmpty(termFrequency) ? Double.parseDouble(termFrequency) : null;
	}

	public Integer getTermOccurrence(String term)
	{
		String occurrence = getAttributeValue(term, TermFrequencyEntityMetaData.OCCURRENCE);
		return StringUtils.isNotEmpty(occurrence) ? Integer.parseInt(occurrence) : null;
	}

	public String getAttributeValue(String term, String attributeName)
	{
		Entity entity = dataService.findOne(TermFrequencyEntityMetaData.ENTITY_NAME,
				new QueryImpl().eq(TermFrequencyEntityMetaData.TERM, term));

		if (entity == null)
		{
			entity = addEntry(term, pubMedTermFrequencyService.getTermFrequency(term), dataService);
		}

		return entity == null ? null : entity.getString(attributeName);
	}

	private Entity addEntry(String term, PubMedTFEntity pubMedTFEntity, DataService dataService)
	{
		if (pubMedTFEntity == null) return null;

		MapEntity mapEntity = new MapEntity();
		mapEntity.set(TermFrequencyEntityMetaData.TERM, term);
		mapEntity.set(TermFrequencyEntityMetaData.FREQUENCY, pubMedTFEntity.getFrequency());
		mapEntity.set(TermFrequencyEntityMetaData.OCCURRENCE, pubMedTFEntity.getOccurrence());
		dataService.add(TermFrequencyEntityMetaData.ENTITY_NAME, mapEntity);
		return mapEntity;
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
