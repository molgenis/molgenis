package org.molgenis.ontology.sorta.job;

import static org.molgenis.ontology.sorta.meta.OntologyTermHitEntityMetaData.SCORE;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.collect.Iterables;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.sorta.meta.MatchingTaskContentEntityMetaData;
import org.molgenis.ontology.sorta.service.SortaService;
import org.molgenis.security.core.runas.RunAsSystemProxy;

import static java.util.Objects.requireNonNull;

public class SortaJobProcessor
{
	private static final int ADD_BATCH_SIZE = 1000;
	private static final int PROGRESS_UPDATE_BATCH_SIZE = 50;

	private final String ontologyIri;
	private final String entityName;
	private final Progress progress;
	private final DataService dataService;
	private final SortaService sortaService;
	private final IdGenerator idGenerator;
	private final AtomicInteger counter;

	public SortaJobProcessor(String ontologyIri, String entityName, Progress progress, DataService dataService,
			SortaService sortaService, IdGenerator idGenerator)
	{
		this.ontologyIri = requireNonNull(ontologyIri);
		this.entityName = requireNonNull(entityName);
		this.progress = requireNonNull(progress);
		this.dataService = requireNonNull(dataService);
		this.sortaService = requireNonNull(sortaService);
		this.idGenerator = requireNonNull(idGenerator);
		this.counter = new AtomicInteger(0);
	}

	public void process()
	{
		RunAsSystemProxy.runAsSystem(() -> {

			long maxCount = dataService.count(entityName, new QueryImpl());

			progress.setProgressMax((int) maxCount);
			// Match input terms with code
			List<Entity> entitiesToAdd = new ArrayList<Entity>();
			dataService.findAll(entityName).forEach(entity -> {
				MapEntity matchingTaskContentEntity = new MapEntity();
				matchingTaskContentEntity.set(MatchingTaskContentEntityMetaData.INPUT_TERM, entity.getIdValue());
				matchingTaskContentEntity.set(MatchingTaskContentEntityMetaData.IDENTIFIER, idGenerator.generateId());
				matchingTaskContentEntity.set(MatchingTaskContentEntityMetaData.REF_ENTITY, entityName);
				matchingTaskContentEntity.set(MatchingTaskContentEntityMetaData.VALIDATED, false);
				entitiesToAdd.add(matchingTaskContentEntity);

				Iterable<Entity> ontologyTermEntities = sortaService.findOntologyTermEntities(ontologyIri, entity);
				if (Iterables.size(ontologyTermEntities) > 0)
				{
					Entity firstMatchedOntologyTerm = Iterables.getFirst(ontologyTermEntities, new MapEntity());
					matchingTaskContentEntity.set(MatchingTaskContentEntityMetaData.MATCHED_TERM,
							firstMatchedOntologyTerm.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI));
					matchingTaskContentEntity.set(MatchingTaskContentEntityMetaData.SCORE,
							firstMatchedOntologyTerm.get(SCORE));
				}
				else
				{
					matchingTaskContentEntity.set(MatchingTaskContentEntityMetaData.SCORE, 0);
				}

				// Add entity in batch
				if (entitiesToAdd.size() >= ADD_BATCH_SIZE)
				{
					dataService.add(MatchingTaskContentEntityMetaData.ENTITY_NAME, entitiesToAdd.stream());
					entitiesToAdd.clear();
				}

				// Increase the number of the progress
				counter.incrementAndGet();

				// Update the progress only when the progress proceeds the threshold
				if (counter.get() % PROGRESS_UPDATE_BATCH_SIZE == 0)
				{
					progress.progress(counter.get(), StringUtils.EMPTY);
				}
			});
			// Add the rest
			if (entitiesToAdd.size() != 0)
			{
				dataService.add(MatchingTaskContentEntityMetaData.ENTITY_NAME, entitiesToAdd.stream());
				entitiesToAdd.clear();
			}
			progress.progress(counter.get(), StringUtils.EMPTY);
			dataService.getRepository(MatchingTaskContentEntityMetaData.ENTITY_NAME).flush();
		});
	}
}