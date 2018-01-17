package org.molgenis.data.annotation.web;

import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.core.EffectCreatingAnnotator;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.exception.SecondRunNotSupportedException;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.permission.PermissionSystemService;
import org.molgenis.i18n.CodedRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Iterator;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.DatabaseAction.UPDATE;
import static org.molgenis.data.RepositoryCapability.WRITABLE;
import static org.molgenis.data.annotation.core.utils.AnnotatorUtils.addAnnotatorMetaDataToRepositories;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;

@Component
public class CrudRepositoryAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(CrudRepositoryAnnotator.class);

	private final DataService dataService;
	private final PermissionSystemService permissionSystemService;
	private EntityType targetMetaData;
	private AttributeFactory attributeFactory;

	public CrudRepositoryAnnotator(DataService dataService, PermissionSystemService permissionSystemService,
			AttributeFactory attributeFactory)
	{
		this.dataService = dataService;
		this.permissionSystemService = permissionSystemService;
		this.attributeFactory = attributeFactory;
	}

	public void annotate(RepositoryAnnotator annotator, Repository<Entity> repository)
	{
		annotate(annotator, repository, UPDATE);
	}

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
					try (Repository externalRepository = dataService.getMeta().createRepository(targetMetaData))
					{
						permissionSystemService.giveUserWriteMetaPermissions(targetMetaData);
						runAsSystem(() -> dataService.getMeta().updateEntityType(externalRepository.getEntityType()));

						iterateOverEntitiesAndAnnotate(repository, annotator, DatabaseAction.ADD);
					}
				}
				else
				{
					throw new SecondRunNotSupportedException(annotator);
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
		catch (CodedRuntimeException crte)
		{
			throw crte;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
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