package org.molgenis.data.annotation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.annotation.utils.AnnotatorUtils;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.security.user.UserAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

public class CrudRepositoryAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(CrudRepositoryAnnotator.class);
	private static final int BATCH_SIZE = 100;

	private final String newRepositoryLabel;
	private final DataService dataService;
	private final PermissionSystemService permissionSystemService;
	private final UserAccountService userAccountService;

	public CrudRepositoryAnnotator(DataService dataService, String newRepositoryName,
			PermissionSystemService permissionSystemService, UserAccountService userAccountService)
	{
		this.dataService = dataService;
		this.newRepositoryLabel = newRepositoryName;
		this.permissionSystemService = permissionSystemService;
		this.userAccountService = userAccountService;
	}

	/**
	 * @param annotators
	 * @param repo
	 * @param createCopy
	 */
	public void annotate(List<RepositoryAnnotator> annotators, Repository repo, boolean createCopy) throws IOException
	{
		for (RepositoryAnnotator annotator : annotators)
		{
			repo = annotate(annotator, repo, createCopy);
			createCopy = false;
		}
	}

	/**
	 * @param annotator
	 * @param sourceRepo
	 * @param createCopy
	 */
	@Transactional
	public Repository annotate(RepositoryAnnotator annotator, Repository sourceRepo, boolean createCopy)
			throws IOException
	{
		if (!sourceRepo.getCapabilities().contains(RepositoryCapability.UPDATEABLE) && !createCopy)
		{
			throw new UnsupportedOperationException("Currently only updateable repositories can be annotated");
		}

		if (createCopy) LOG.info("Creating a copy of " + sourceRepo.getName() + " repository, which will be labelled "
				+ newRepositoryLabel + ". A UUID will be generated for the name/identifier");

		LOG.info("Started annotating \"" + sourceRepo.getName() + "\" with the " + annotator.getSimpleName()
				+ " annotator (started by \"" + userAccountService.getCurrentUser().getUsername() + "\")");

		EntityMetaData entityMetaData = dataService.getMeta().getEntityMetaData(sourceRepo.getName());
		DefaultAttributeMetaData compoundAttributeMetaData = AnnotatorUtils.getCompoundResultAttribute(annotator,
                entityMetaData);

		Repository targetRepo = addAnnotatorMetadataToRepositories(entityMetaData, createCopy,
				compoundAttributeMetaData);

		Repository crudRepository = iterateOverEntitiesAndAnnotate(sourceRepo, targetRepo, annotator);

		LOG.info("Finished annotating \"" + sourceRepo.getName() + "\" with the " + annotator.getSimpleName()
				+ " annotator (started by \"" + userAccountService.getCurrentUser().getUsername() + "\")");

		return crudRepository;
	}

	/**
	 * Iterates over all the entities within a repository and annotates.
	 */
	private Repository iterateOverEntitiesAndAnnotate(Repository sourceRepo, Repository targetRepo,
			RepositoryAnnotator annotator)
	{
		Iterator<Entity> entityIterator = annotator.annotate(sourceRepo);
		List<Entity> annotatedEntities = new ArrayList<>();
		int i = 0;

		if (targetRepo == null)
		{
			// annotate repository to itself
			while (entityIterator.hasNext())
			{
				Entity entity = entityIterator.next();
				annotatedEntities.add(entity);
				if (annotatedEntities.size() == BATCH_SIZE)
				{
					dataService.update(sourceRepo.getName(), annotatedEntities);
					i = i + annotatedEntities.size();
					LOG.info("annotated " + i + " \"" + sourceRepo.getName() + "\" entities with the "
							+ annotator.getSimpleName() + " annotator (started by \""
							+ userAccountService.getCurrentUser().getUsername() + "\")");
					annotatedEntities.clear();
				}
			}
			if (annotatedEntities.size() > 0)
			{
				dataService.update(sourceRepo.getName(), annotatedEntities);
				i = i + annotatedEntities.size();
				LOG.info("annotated " + i + " \"" + sourceRepo.getName() + "\" entities with the "
						+ annotator.getSimpleName() + " annotator (started by \""
						+ userAccountService.getCurrentUser().getUsername() + "\")");
				annotatedEntities.clear();
			}
			return dataService.getRepository(sourceRepo.getName());
		}
		else
		{
			// annotate from source to target repository
			while (entityIterator.hasNext())
			{
				Entity entity = entityIterator.next();
				annotatedEntities.add(entity);
				if (annotatedEntities.size() == BATCH_SIZE)
				{
					targetRepo.add(annotatedEntities);
					i = i + annotatedEntities.size();
					LOG.info("annotated " + i + " \"" + sourceRepo.getName() + "\" entities with the "
							+ annotator.getSimpleName() + " annotator (started by \""
							+ userAccountService.getCurrentUser().getUsername() + "\")");
					annotatedEntities.clear();
				}
			}
			if (annotatedEntities.size() > 0)
			{
				dataService.add(targetRepo.getName(), annotatedEntities);
				i = i + annotatedEntities.size();
				LOG.info("annotated " + i + " \"" + sourceRepo.getName() + "\" entities with the "
						+ annotator.getSimpleName() + " annotator (started by \""
						+ userAccountService.getCurrentUser().getUsername() + "\")");
				annotatedEntities.clear();
			}
			return targetRepo;
		}
	}

	/**
	 * Adds a new compound attribute to an existing mysql CrudRepository which is part of the
	 * {@link #mysqlRepositoryCollection} or an existing CrudRepository which is not part of
	 * {@link #mysqlRepositoryCollection}.
	 *
	 * @param entityMetaData
	 *            {@link EntityMetaData} for the existing repository
	 * @param createCopy
	 * @param compoundAttributeMetaData
	 */
	public Repository addAnnotatorMetadataToRepositories(EntityMetaData entityMetaData, boolean createCopy,
			DefaultAttributeMetaData compoundAttributeMetaData)
	{
		if (createCopy)
		{
			DefaultEntityMetaData newEntityMetaData = new DefaultEntityMetaData(RandomStringUtils.randomAlphanumeric(30),
					entityMetaData);
			if (newEntityMetaData.getAttribute(compoundAttributeMetaData.getName()) == null)
			{
				newEntityMetaData.addAttributeMetaData(compoundAttributeMetaData);
			}
			newEntityMetaData.setLabel(newRepositoryLabel);

			// Give current user permissions on the created repo
			permissionSystemService.giveUserEntityPermissions(SecurityContextHolder.getContext(),
					Arrays.asList(newEntityMetaData.getName()));

			return dataService.getMeta().addEntityMeta(newEntityMetaData);
		}
		else if (entityMetaData.getAttribute(compoundAttributeMetaData.getName()) == null)
		{
			DefaultEntityMetaData newEntityMetaData = new DefaultEntityMetaData(entityMetaData);
			newEntityMetaData.addAttributeMetaData(compoundAttributeMetaData);
			dataService.getMeta().updateSync(newEntityMetaData);
		}

		return null;
	}
}