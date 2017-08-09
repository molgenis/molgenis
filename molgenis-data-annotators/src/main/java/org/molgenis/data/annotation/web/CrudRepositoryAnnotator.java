package org.molgenis.data.annotation.web;

import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.core.EffectCreatingAnnotator;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.exception.AnnotationException;
import org.molgenis.data.annotation.core.exception.UiAnnotationException;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.permission.PermissionSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Iterator;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.DatabaseAction.UPDATE;
import static org.molgenis.data.RepositoryCapability.WRITABLE;
import static org.molgenis.data.annotation.core.utils.AnnotatorUtils.addAnnotatorMetaDataToRepositories;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

@Component
public class CrudRepositoryAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(CrudRepositoryAnnotator.class);

	private final DataService dataService;
	private final PermissionSystemService permissionSystemService;
	private EntityType targetMetaData;
	private AttributeFactory attributeFactory;

	@Autowired
	public CrudRepositoryAnnotator(DataService dataService, PermissionSystemService permissionSystemService,
			AttributeFactory attributeFactory)
	{
		this.dataService = dataService;
		this.permissionSystemService = permissionSystemService;
		this.attributeFactory = attributeFactory;
	}

	/**
	 * @param annotator
	 * @param repository
	 */
	public void annotate(RepositoryAnnotator annotator, Repository<Entity> repository) throws IOException
	{
		annotate(annotator, repository, UPDATE);
	}

	/**
	 * @param annotator
	 * @param repository
	 * @param action
	 */
	private void annotate(RepositoryAnnotator annotator, Repository<Entity> repository, DatabaseAction action)
	{
		if (!repository.getCapabilities().contains(WRITABLE))
		{
			throw new UnsupportedOperationException("Currently only writable repositories can be annotated");
		}
		try
		{
			EntityType entityType = dataService.getMeta().getEntityType(repository.getName());

			if (annotator instanceof EffectCreatingAnnotator)
			{
				targetMetaData = ((EffectCreatingAnnotator) annotator).getTargetEntityType(entityType);
				if (!dataService.hasRepository(targetMetaData.getId()))
				{
					// add new entities to new repo
					Repository externalRepository = dataService.getMeta().createRepository(targetMetaData);
					permissionSystemService.giveUserWriteMetaPermissions(targetMetaData);
					runAsSystem(() -> dataService.getMeta().updateEntityType(externalRepository.getEntityType()));

					iterateOverEntitiesAndAnnotate(repository, annotator, DatabaseAction.ADD);
				}
				else
				{
					throw new UnsupportedOperationException(
							"This entity has already been annotated with " + annotator.getSimpleName());
				}
			}
			else
			{
				runAsSystem(() -> dataService.getMeta()
											 .updateEntityType(
													 addAnnotatorMetaDataToRepositories(entityType, attributeFactory,
															 annotator)));

				iterateOverEntitiesAndAnnotate(dataService.getRepository(repository.getName()), annotator, action);
			}
		}
		catch (AnnotationException ae)
		{
			deleteResultEntity(annotator, targetMetaData);
			throw new UiAnnotationException(ae);
		}
		catch (Exception e)
		{
			deleteResultEntity(annotator, targetMetaData);
			throw new RuntimeException(e);
		}
	}

	private void deleteResultEntity(RepositoryAnnotator annotator, EntityType targetMetaData)
	{
		try
		{
			if (annotator instanceof EffectCreatingAnnotator && targetMetaData != null)
			{
				runAsSystem(() ->
				{
					dataService.deleteAll(targetMetaData.getId());
					dataService.getMeta().deleteEntityType(targetMetaData.getId());
				});
			}
		}
		catch (Exception ex)
		{
			// log the problem but throw the original exception
			LOG.error("Failed to remove result entity: %s", targetMetaData.getId());
		}
	}

	/**
	 * Iterates over all the entities within a repository and annotates.
	 */
	private void iterateOverEntitiesAndAnnotate(Repository<Entity> repository, RepositoryAnnotator annotator,
			DatabaseAction action)
	{
		Iterator<Entity> it = annotator.annotate(repository);

		String entityTypeId;
		if (annotator instanceof EffectCreatingAnnotator)
		{
			entityTypeId = ((EffectCreatingAnnotator) annotator).getTargetEntityType(repository.getEntityType())
																.getId();
		}
		else
		{
			entityTypeId = repository.getName();
		}
		switch (action)
		{
			case UPDATE:
				dataService.update(entityTypeId, stream(spliteratorUnknownSize(it, ORDERED), false));
				break;
			case ADD:
				dataService.add(entityTypeId, stream(spliteratorUnknownSize(it, ORDERED), false));
				break;
			default:
				throw new UnsupportedOperationException();
		}
	}
}