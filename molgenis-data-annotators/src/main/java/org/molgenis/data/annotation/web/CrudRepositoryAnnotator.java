package org.molgenis.data.annotation.web;

import org.molgenis.data.*;
import org.molgenis.data.annotation.core.RefEntityAnnotator;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.exception.AnnotationException;
import org.molgenis.data.annotation.core.exception.UiAnnotationException;
import org.molgenis.data.annotation.core.utils.AnnotatorUtils;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.security.permission.PermissionSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

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
		annotate(annotator, repository, DatabaseAction.UPDATE);
	}

	/**
	 * @param annotator
	 * @param repository
	 * @param action
	 */
	private void annotate(RepositoryAnnotator annotator, Repository<Entity> repository, DatabaseAction action)
	{
		if (!repository.getCapabilities().contains(RepositoryCapability.WRITABLE))
		{
			throw new UnsupportedOperationException("Currently only writable repositories can be annotated");
		}
		try
		{
			EntityType entityType = dataService.getMeta().getEntityType(repository.getName());

			if (annotator instanceof RefEntityAnnotator)
			{
				targetMetaData = ((RefEntityAnnotator) annotator).getTargetEntityType(entityType);
				if (!dataService.hasRepository(targetMetaData.getName()))
				{
					// add new entities to new repo
					Repository externalRepository = dataService.getMeta().createRepository(targetMetaData);
					permissionSystemService.giveUserEntityPermissions(SecurityContextHolder.getContext(),
							Collections.singletonList(externalRepository.getName()));
					RunAsSystemProxy.runAsSystem(
							() -> dataService.getMeta().updateEntityType(externalRepository.getEntityType()));

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
				RunAsSystemProxy.runAsSystem(() -> dataService.getMeta().updateEntityType(
						AnnotatorUtils.addAnnotatorMetaDataToRepositories(entityType, attributeFactory, annotator)));

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
			if (annotator instanceof RefEntityAnnotator && targetMetaData != null)
			{
				RunAsSystemProxy.runAsSystem(() ->
				{
					dataService.deleteAll(targetMetaData.getName());
					dataService.getMeta().deleteEntityType(targetMetaData.getName());
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
	private void iterateOverEntitiesAndAnnotate(Repository<Entity> repository, RepositoryAnnotator annotator,
			DatabaseAction action)
	{
		Iterator<Entity> it = annotator.annotate(repository);

		String entityName;
		if (annotator instanceof RefEntityAnnotator)
		{
			entityName = ((RefEntityAnnotator) annotator).getTargetEntityType(repository.getEntityType()).getName();
		}
		else
		{
			entityName = repository.getName();
		}
		switch (action)
		{
			case UPDATE:
				dataService.update(entityName,
						StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false));
				break;
			case ADD:
				dataService.add(entityName,
						StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false));
				break;
			default:
				throw new UnsupportedOperationException();
		}
	}
}