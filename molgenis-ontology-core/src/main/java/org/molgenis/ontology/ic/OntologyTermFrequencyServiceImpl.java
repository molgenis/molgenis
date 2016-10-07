package org.molgenis.ontology.ic;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.meta.OntologyTermSynonymMetaData;
import org.molgenis.ontology.core.meta.TermFrequency;
import org.molgenis.ontology.core.meta.TermFrequencyFactory;
import org.molgenis.ontology.core.meta.TermFrequencyMetaData;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class OntologyTermFrequencyServiceImpl implements TermFrequencyService
{
	private final static int BATCH_SIZE = 10000;
	private final static float DEFAULT_BOOSTED_VALUE = 1.0f;
	private final PubMedTermFrequencyService pubMedTermFrequencyService = new PubMedTermFrequencyService();
	private final DataService dataService;
	private final TermFrequencyFactory termFrequencyFactory;

	@Autowired
	public OntologyTermFrequencyServiceImpl(DataService dataService, TermFrequencyFactory termFrequencyFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.termFrequencyFactory = requireNonNull(termFrequencyFactory);
	}

	@Override
	public float getTermFrequency(String term)
	{
		TermFrequency termFrequencyEntity = getTermFrequencyEntity(term);
		return termFrequencyEntity != null ? termFrequencyEntity.getFrequency().floatValue() : DEFAULT_BOOSTED_VALUE;
	}

	@Override
	public int getTermOccurrence(String term)
	{
		TermFrequency termFrequencyEntity = getTermFrequencyEntity(term);
		return termFrequencyEntity != null ? termFrequencyEntity.getOccurrence() : 0;
	}

	private TermFrequency getTermFrequencyEntity(String term)
	{
		if (StringUtils.isBlank(term)) return null;

		TermFrequency termFrequency = dataService.findOne(TermFrequencyMetaData.TERM_FREQUENCY,
				new QueryImpl<TermFrequency>().eq(TermFrequencyMetaData.TERM, term), TermFrequency.class);

		if (termFrequency == null)
		{
			termFrequency = addEntry(term, pubMedTermFrequencyService.getTermFrequency(term), dataService);
		}

		return termFrequency;
	}

	private TermFrequency addEntry(String term, PubMedTFEntity pubMedTFEntity, DataService dataService)
	{
		if (pubMedTFEntity == null) return null;

		TermFrequency termFrequency = termFrequencyFactory.create();
		termFrequency.setTerm(term);
		termFrequency.setOccurrence(pubMedTFEntity.getOccurrence());
		termFrequency.setFrequency(pubMedTFEntity.getFrequency());

		dataService.add(TermFrequencyMetaData.TERM_FREQUENCY, termFrequency);

		return termFrequency;
	}

	@Override
	@Async
	@RunAsSystem
	public void updateTermFrequency()
	{
		// Remove all the existing term frequency records
		dataService.deleteAll(TermFrequencyMetaData.TERM_FREQUENCY);

		List<Entity> entitiesToAdd = new ArrayList<Entity>();
		// Create new term frequency records
		dataService.findAll(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM).forEach(entity ->
		{

			String ontologyTermSynonym = entity.getString(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM);

			PubMedTFEntity pubMedTFEntity = pubMedTermFrequencyService.getTermFrequency(ontologyTermSynonym);

			if (pubMedTFEntity != null)
			{
				TermFrequency termFrequency = termFrequencyFactory.create();
				termFrequency.setTerm(ontologyTermSynonym);
				termFrequency.setOccurrence(pubMedTFEntity.getOccurrence());
				termFrequency.setFrequency(pubMedTFEntity.getFrequency());

				entitiesToAdd.add(termFrequency);

				if (entitiesToAdd.size() > BATCH_SIZE)
				{
					dataService.add(TermFrequencyMetaData.TERM_FREQUENCY, entitiesToAdd.stream());
					entitiesToAdd.clear();
				}
			}
		});

		if (entitiesToAdd.size() != 0)
		{
			dataService.add(TermFrequencyMetaData.TERM_FREQUENCY, entitiesToAdd.stream());
		}
	}
}
