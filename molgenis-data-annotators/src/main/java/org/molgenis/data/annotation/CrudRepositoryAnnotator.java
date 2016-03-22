package org.molgenis.data.annotation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

@Component
public class CrudRepositoryAnnotator
{
	private enum RepositoryAction
	{
		ADD, UPDATE, UPSERT
	}

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

			Repository crudRepository;
			if (annotator instanceof AbstractExternalRepositoryAnnotator)
			{
				AbstractExternalRepositoryAnnotator externalAnnotator = (AbstractExternalRepositoryAnnotator) annotator;
				EntityMetaData targetMetaData = externalAnnotator.getOutputMetaData(entityMetaData);

				if (dataService.getMeta().getEntityMetaData(targetMetaData.getName()) == null)
				{
					// add new entities to new repo
					Repository externalRepository = dataService.getMeta().addEntityMeta(targetMetaData);

					crudRepository = iterateOverEntitiesAndAnnotate(repository, externalRepository, externalAnnotator,
							RepositoryAction.ADD);
				}
				else
				{
					// upsert entities to existing repo
					crudRepository = null;
				}

			}
			else
			{
				// add attribute meta data to source entity
				DefaultAttributeMetaData compoundAttributeMetaData = AnnotatorUtils
						.getCompoundResultAttribute(annotator, entityMetaData);

				RunAsSystemProxy.runAsSystem(
						() -> addAnnotatorMetadataToRepositories(entityMetaData, compoundAttributeMetaData));
				crudRepository = iterateOverEntitiesAndAnnotate(repository, repository, annotator,
						RepositoryAction.UPDATE);
			}

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
	private Repository iterateOverEntitiesAndAnnotate(Repository sourceRepository, Repository targetRepository,
			RepositoryAnnotator annotator, RepositoryAction action)
	{
		Iterator<Entity> it = annotator.annotate(sourceRepository);

		List<Entity> batch = new ArrayList<>();
		while (it.hasNext())
		{
			batch.add(it.next());
			if (batch.size() == BATCH_SIZE)
			{
				processBatch(batch, targetRepository, action);
				batch.clear();
			}
		}

		if (!batch.isEmpty())
		{
			processBatch(batch, targetRepository, action);
		}

		return targetRepository;
	}

	private void processBatch(List<Entity> batch, Repository repository, RepositoryAction action)
	{
		switch (action)
		{
			case UPDATE:
				repository.update(batch.stream());
				break;
			case ADD:
				repository.add(batch.stream());
			case UPSERT:
				// upsertBatch(batch);
			default:
				break;
		}

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

			// traverseCompound(compoundAttributeMetaData);

			dataService.getMeta().updateSync(newEntityMetaData);
		}
	}

	// private void traverseCompound(AttributeMetaData compound)
	// {
	// for (AttributeMetaData attr : compound.getAttributeParts())
	// {
	// if (attr.getDataType().equals(COMPOUND))
	// {
	// traverseCompound(attr);
	// }
	// else if (attr.getDataType().equals(MolgenisFieldTypes.CATEGORICAL)
	// || attr.getDataType().equals(MolgenisFieldTypes.CATEGORICAL_MREF)
	// || attr.getDataType().equals(MolgenisFieldTypes.MREF)
	// || attr.getDataType().equals(MolgenisFieldTypes.XREF))
	// {
	//
	// }
	//
	// }
	// }

}