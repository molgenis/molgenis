package org.molgenis.data.annotation;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.annotation.utils.AnnotatorUtils;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class CrudRepositoryAnnotator
{
	private static final int BATCH_SIZE = 1000;

	private final DataService dataService;

	@Autowired
	public CrudRepositoryAnnotator(DataService dataService)
	{
		this.dataService = dataService;
	}

	/**
	 * @param annotators
	 * @param repo
	 */
	public void annotate(List<RepositoryAnnotator> annotators, Repository repo) throws IOException
	{
		for (RepositoryAnnotator annotator : annotators)
		{
			repo = annotate(annotator, repo);
		}
	}

	/**
	 * @param annotator
	 * @param repository
	 */
	@Transactional
	public Repository annotate(RepositoryAnnotator annotator, Repository repository) throws IOException
	{
		if (!repository.getCapabilities().contains(RepositoryCapability.WRITABLE))
		{
			throw new UnsupportedOperationException("Currently only writable repositories can be annotated");
		}
		try
		{
			EntityMetaData entityMetaData = dataService.getMeta().getEntityMetaData(repository.getName());
			DefaultAttributeMetaData compoundAttributeMetaData = AnnotatorUtils.getCompoundResultAttribute(annotator,
					entityMetaData);

			RunAsSystemProxy
					.runAsSystem(() -> addAnnotatorMetadataToRepositories(entityMetaData, compoundAttributeMetaData));

			Repository crudRepository = iterateOverEntitiesAndAnnotate(repository, annotator);
			return crudRepository;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Iterates over all the entities within a repository and annotates.
	 */
	private Repository iterateOverEntitiesAndAnnotate(Repository repository, RepositoryAnnotator annotator)
	{
		Iterator<Entity> it = annotator.annotate(repository);

		List<Entity> batch = new ArrayList<>();
		while (it.hasNext())
		{
			batch.add(it.next());
			if (batch.size() == BATCH_SIZE)
			{
				processBatch(batch, repository);
				batch.clear();
			}
		}

		if (!batch.isEmpty())
		{
			processBatch(batch, repository);
		}

		return repository;
	}

	private void processBatch(List<Entity> batch, Repository repository)
	{
		repository.update(batch.stream());
	}

	/**
	 * Adds a new compound attribute to an existing CrudRepository
	 *
	 * @param entityMetaData
	 *            {@link EntityMetaData} for the existing repository
	 * @param compoundAttributeMetaData
	 */
	private void addAnnotatorMetadataToRepositories(EntityMetaData entityMetaData,
			DefaultAttributeMetaData compoundAttributeMetaData)
	{
		if (entityMetaData.getAttribute(compoundAttributeMetaData.getName()) == null)
		{
			DefaultEntityMetaData newEntityMetaData = new DefaultEntityMetaData(entityMetaData);
			newEntityMetaData.addAttributeMetaData(compoundAttributeMetaData);
			dataService.getMeta().updateSync(newEntityMetaData);
		}
	}

}