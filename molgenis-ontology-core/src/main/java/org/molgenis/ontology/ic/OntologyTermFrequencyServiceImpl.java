package org.molgenis.ontology.ic;

import static org.molgenis.ontology.core.meta.OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM;
import static org.molgenis.ontology.ic.TermFrequencyEntityMetaData.TERM_FREQUENCY;
import static org.molgenis.util.ApplicationContextProvider.getApplicationContext;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DynamicEntity;
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

	@Override
	public Double getTermFrequency(String term)
	{
		String termFrequency = getAttributeValue(term, TermFrequencyEntityMetaData.FREQUENCY);
		return StringUtils.isNotEmpty(termFrequency) ? Double.parseDouble(termFrequency) : null;
	}

	@Override
	public Integer getTermOccurrence(String term)
	{
		String occurrence = getAttributeValue(term, TermFrequencyEntityMetaData.OCCURRENCE);
		return StringUtils.isNotEmpty(occurrence) ? Integer.parseInt(occurrence) : null;
	}

	public String getAttributeValue(String term, String attributeName)
	{
		Entity entity = dataService.findOne(TERM_FREQUENCY,
				new QueryImpl<Entity>().eq(TermFrequencyEntityMetaData.TERM, term));

		if (entity == null)
		{
			entity = addEntry(term, pubMedTermFrequencyService.getTermFrequency(term), dataService);
		}

		return entity == null ? null : entity.getString(attributeName);
	}

	private Entity addEntry(String term, PubMedTFEntity pubMedTFEntity, DataService dataService)
	{
		if (pubMedTFEntity == null) return null;

		// FIXME remove reference to getApplicationContext
		TermFrequencyEntityMetaData termFrequencyEntityMeta = getApplicationContext()
				.getBean(TermFrequencyEntityMetaData.class);
		Entity entity = new DynamicEntity(termFrequencyEntityMeta);
		entity.set(TermFrequencyEntityMetaData.TERM, term);
		entity.set(TermFrequencyEntityMetaData.FREQUENCY, pubMedTFEntity.getFrequency());
		entity.set(TermFrequencyEntityMetaData.OCCURRENCE, pubMedTFEntity.getOccurrence());
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

		List<Entity> entitiesToAdd = new ArrayList<Entity>();
		// Create new term frequency records
		dataService.findAll(ONTOLOGY_TERM_SYNONYM).forEach(entity -> {
			String ontologyTermSynonym = entity.getString(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR);
			PubMedTFEntity pubMedTFEntity = pubMedTermFrequencyService.getTermFrequency(ontologyTermSynonym);

			if (pubMedTFEntity != null)
			{
				// FIXME remove reference to getApplicationContext
				TermFrequencyEntityMetaData termFrequencyEntityMeta = getApplicationContext()
						.getBean(TermFrequencyEntityMetaData.class);
				Entity mapEntity = new DynamicEntity(termFrequencyEntityMeta);
				mapEntity.set(TermFrequencyEntityMetaData.TERM, ontologyTermSynonym);
				mapEntity.set(TermFrequencyEntityMetaData.FREQUENCY, pubMedTFEntity.getFrequency());
				mapEntity.set(TermFrequencyEntityMetaData.OCCURRENCE, pubMedTFEntity.getOccurrence());
				entitiesToAdd.add(mapEntity);

				if (entitiesToAdd.size() > BATCH_SIZE)
				{
					dataService.add(TERM_FREQUENCY, entitiesToAdd.stream());
					entitiesToAdd.clear();
				}
			}
		});

		if (entitiesToAdd.size() != 0)
		{
			dataService.add(TERM_FREQUENCY, entitiesToAdd.stream());
		}
	}
}
