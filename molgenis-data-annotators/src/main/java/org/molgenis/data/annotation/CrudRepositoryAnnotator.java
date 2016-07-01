package org.molgenis.data.annotation;

import org.molgenis.data.*;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.security.permission.PermissionSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.molgenis.MolgenisFieldTypes.AttributeType.COMPOUND;

@Component
public class CrudRepositoryAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(CrudRepositoryAnnotator.class);

	private static final int BATCH_SIZE = 1000;

	private final DataService dataService;
	private final PermissionSystemService permissionSystemService;

	@Autowired
	public EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	public AttributeMetaDataFactory attributeMetaDataFactory;

	@Autowired
	public VcfAttributes vcfAttributes;
	private EntityMetaData targetMetaData;

	@Autowired
	public CrudRepositoryAnnotator(DataService dataService, PermissionSystemService permissionSystemService)
	{
		this.dataService = dataService;
		this.permissionSystemService = permissionSystemService;
	}

	/**
	 * @param annotator
	 * @param repository
	 */
	public Repository<Entity> annotate(RepositoryAnnotator annotator, Repository<Entity> repository) throws IOException
	{
		return annotate(annotator, repository, DatabaseAction.UPDATE);
	}

	/**
	 * @param annotator
	 * @param repository
	 * @param action
	 */
	public Repository<Entity> annotate(RepositoryAnnotator annotator, Repository<Entity> repository,
			DatabaseAction action) throws IOException
	{
		if (!repository.getCapabilities().contains(RepositoryCapability.WRITABLE))
		{
			throw new UnsupportedOperationException("Currently only writable repositories can be annotated");
		}
		try
		{
			EntityMetaData entityMetaData = dataService.getMeta().getEntityMetaData(repository.getName());
			List<AttributeMetaData> attributeMetaDatas = annotator.getOutputAttributes();

			RunAsSystemProxy.runAsSystem(
					() -> addAnnotatorMetadataToRepositories(entityMetaData, annotator.getSimpleName(),
							attributeMetaDatas));
			Repository<Entity> crudRepository;
			if (annotator instanceof RefEntityAnnotator)
			{
				targetMetaData = ((RefEntityAnnotator) annotator).getOutputAttributes(entityMetaData);
				if (!dataService.hasRepository(targetMetaData.getName()))
				{
					// add new entities to new repo
					Repository externalRepository = dataService.getMeta().addEntityMeta(targetMetaData);
					permissionSystemService.giveUserEntityPermissions(SecurityContextHolder.getContext(),
							Collections.singletonList(externalRepository.getName()));
					crudRepository = iterateOverEntitiesAndAnnotate(repository, annotator, DatabaseAction.ADD);
				}
				else
				{
					throw new UnsupportedOperationException(
							"This entity has already been annotated with " + annotator.getSimpleName());
				}
			}
			else
			{
				RunAsSystemProxy.runAsSystem(
						() -> addAnnotatorMetadataToRepositories(entityMetaData, annotator.getSimpleName(),
								attributeMetaDatas));

				crudRepository = iterateOverEntitiesAndAnnotate(dataService.getRepository(repository.getName()),
						annotator, action);
			}
			return crudRepository;
		}
		catch (Exception e)
		{
			deleteResultEntity(annotator, targetMetaData);
			throw new RuntimeException(e);
		}
	}

	private void deleteResultEntity(RepositoryAnnotator annotator, EntityMetaData targetMetaData)
	{
		try
		{
			if (annotator instanceof RefEntityAnnotator && targetMetaData != null)
			{
				RunAsSystemProxy.runAsSystem(() -> {
					dataService.deleteAll(targetMetaData.getName());
					dataService.getMeta().deleteEntityMeta(targetMetaData.getName());
				});
			}
		}
		catch (Exception ex)
		{
			// log the problem but throw the original exception
			LOG.error("Failed to remove result entity: %s", targetMetaData.getName());
		}
	}

	/**
	 * Iterates over all the entities within a repository and annotates.
	 */
	private Repository<Entity> iterateOverEntitiesAndAnnotate(Repository<Entity> repository,
			RepositoryAnnotator annotator, DatabaseAction action)
	{
		Iterator<Entity> it = annotator.annotate(repository);

		List<Entity> batch = new ArrayList<>();
		while (it.hasNext())
		{
			batch.add(it.next());
			if (batch.size() == BATCH_SIZE)
			{
				processBatch(batch, repository, action);
				batch.clear();
			}
		}

		if (!batch.isEmpty())
		{
			processBatch(batch, repository, action);
		}

		return repository;
	}

	private void processBatch(List<Entity> batch, Repository<Entity> repository, DatabaseAction action)
	{
		switch (action)
		{
			case UPDATE:
				repository.update(batch.stream());
				break;
			case ADD:
				repository.add(batch.stream());
				break;
			default:
				throw new UnsupportedOperationException();
		}

	}

	/**
	 * Adds a new compound attribute to an existing CrudRepository
	 *
	 * @param entityMetaData {@link EntityMetaData} for the existing repository
	 * @param annotatorName
	 */
	private void addAnnotatorMetadataToRepositories(EntityMetaData entityMetaData, String annotatorName,
			List<AttributeMetaData> attributeMetaDatas)
	{
		//FIXME: add the attributes in the compound once the generating of id's in the factory is implemented
		//currently this would nullpointer
		AttributeMetaData compoundAttributeMetaData = attributeMetaDataFactory.create()
				.setName("MOLGENIS_" + annotatorName).setDataType(COMPOUND);
		if (entityMetaData.getAttribute("MOLGENIS_" + annotatorName) == null)
		{
			for (AttributeMetaData part : attributeMetaDatas)
			{
				//compoundAttributeMetaData.addAttributePart(part);
				if (entityMetaData.getAttribute(part.getName()) == null) entityMetaData.addAttribute(part);
			}
			//entityMetaData.addAttribute(compoundAttributeMetaData);
			dataService.getMeta().updateEntityMeta(entityMetaData);
		}
	}

}