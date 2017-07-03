package org.molgenis.ontology.sorta.job;

import com.google.common.collect.Iterables;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.controller.SortaServiceController;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.sorta.meta.MatchingTaskContentMetaData;
import org.molgenis.ontology.sorta.service.SortaService;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.ui.menu.MenuReaderService;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.ontology.sorta.meta.OntologyTermHitMetaData.SCORE;
import static org.molgenis.util.ApplicationContextProvider.getApplicationContext;

public class SortaJobProcessor
{
	private static final int ADD_BATCH_SIZE = 1000;
	private static final int PROGRESS_UPDATE_BATCH_SIZE = 50;

	private final String ontologyIri;
	private final String inputRepositoryName;
	private final String resultRepositoryName;
	private final Progress progress;
	private final DataService dataService;
	private final SortaService sortaService;
	private final IdGenerator idGenerator;
	private final AtomicInteger counter;
	private final MenuReaderService menuReaderService;

	public SortaJobProcessor(String ontologyIri, String inputRepositoryName, String resultRepositoryName,
			Progress progress, DataService dataService, SortaService sortaService, IdGenerator idGenerator,
			MenuReaderService menuReaderService)
	{
		this.ontologyIri = requireNonNull(ontologyIri);
		this.inputRepositoryName = requireNonNull(inputRepositoryName);
		this.resultRepositoryName = requireNonNull(resultRepositoryName);
		this.progress = requireNonNull(progress);
		this.dataService = requireNonNull(dataService);
		this.sortaService = requireNonNull(sortaService);
		this.idGenerator = requireNonNull(idGenerator);
		this.counter = new AtomicInteger(0);
		this.menuReaderService = requireNonNull(menuReaderService);
	}

	public void process()
	{
		RunAsSystemProxy.runAsSystem(() ->
		{
			long maxCount = dataService.count(inputRepositoryName, new QueryImpl<>());
			progress.status(
					"Matching " + maxCount + " input terms from " + inputRepositoryName + ".\nStoring results in "
							+ resultRepositoryName);

			progress.setProgressMax((int) maxCount);

			// FIXME get rid of getApplicationContext reference
			MatchingTaskContentMetaData matchingTaskContentMetaData = getApplicationContext().getBean(
					MatchingTaskContentMetaData.class);

			// Match input terms with code
			List<Entity> entitiesToAdd = newArrayList();
			dataService.findAll(inputRepositoryName).forEach(inputRow ->
			{
				Entity resultEntity = new DynamicEntity(matchingTaskContentMetaData)
				{
					@Override
					protected void validateValueType(String attrName, Object value)
					{
						// FIXME enable validation by not overriding this method
					}
				};
				resultEntity.set(MatchingTaskContentMetaData.INPUT_TERM, inputRow);
				resultEntity.set(MatchingTaskContentMetaData.IDENTIFIER, idGenerator.generateId());
				resultEntity.set(MatchingTaskContentMetaData.VALIDATED, false);
				entitiesToAdd.add(resultEntity);

				Iterable<Entity> ontologyTermEntities = sortaService.findOntologyTermEntities(ontologyIri, inputRow);
				if (Iterables.size(ontologyTermEntities) > 0)
				{
					Entity firstMatchedOntologyTerm = Iterables.getFirst(ontologyTermEntities,
							new DynamicEntity(matchingTaskContentMetaData));
					resultEntity.set(MatchingTaskContentMetaData.MATCHED_TERM,
							firstMatchedOntologyTerm.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI));
					resultEntity.set(MatchingTaskContentMetaData.SCORE, firstMatchedOntologyTerm.get(SCORE));
				}
				else
				{
					resultEntity.set(MatchingTaskContentMetaData.SCORE, 0);
				}

				// Add entity in batch
				if (entitiesToAdd.size() >= ADD_BATCH_SIZE)
				{
					dataService.add(resultRepositoryName, entitiesToAdd.stream());
					entitiesToAdd.clear();
				}

				// Increase the number of the progress
				counter.incrementAndGet();

				// Update the progress only when the progress proceeds the threshold
				if (counter.get() % PROGRESS_UPDATE_BATCH_SIZE == 0)
				{
					progress.progress(counter.get(), "Processed " + counter + " input terms.");
				}
			});
			// Add the rest
			if (entitiesToAdd.size() != 0)
			{
				dataService.add(resultRepositoryName, entitiesToAdd.stream());
			}
			progress.progress(counter.get(), "Processed " + counter + " input terms.");
			progress.setResultUrl(menuReaderService.getMenu().findMenuItemPath(SortaServiceController.ID) + "/result/"
					+ resultRepositoryName);
		});
	}
}