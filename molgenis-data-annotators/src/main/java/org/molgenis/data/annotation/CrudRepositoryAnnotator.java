package org.molgenis.data.annotation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.annotation.utils.AnnotatorUtils;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.security.user.UserAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

public class CrudRepositoryAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(CrudRepositoryAnnotator.class);
	private static final int BATCH_SIZE = 1000;

	private final String newRepositoryLabel;
	private final DataService dataService;
	private final PermissionSystemService permissionSystemService;
	private final UserAccountService userAccountService;
	private final MolgenisPermissionService molgenisPermissionService;

	public CrudRepositoryAnnotator(DataService dataService, String newRepositoryName,
			PermissionSystemService permissionSystemService, UserAccountService userAccountService,
			MolgenisPermissionService molgenisPermissionService)
	{
		this.dataService = dataService;
		this.newRepositoryLabel = newRepositoryName;
		this.permissionSystemService = permissionSystemService;
		this.userAccountService = userAccountService;
		this.molgenisPermissionService = molgenisPermissionService;
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

		if (!molgenisPermissionService.hasPermissionOnEntity(sourceRepo.getName(), Permission.WRITE))
		{
			throw new AccessDeniedException("No write permission on entity '" + sourceRepo.getName() + "'");
		}

		if (createCopy) LOG.info("Creating a copy of " + sourceRepo.getName() + " repository, which will be labelled "
				+ newRepositoryLabel + ". A UUID will be generated for the name/identifier");

		String user = userAccountService.getCurrentUser().getUsername();
		LOG.info("Started annotating \"" + sourceRepo.getName() + "\" with the " + annotator.getSimpleName()
				+ " annotator (started by \"" + user + "\")");

		// Current security context
		SecurityContext securityContext = SecurityContextHolder.getContext();

		return RunAsSystemProxy.runAsSystem(() -> {
			EntityMetaData entityMetaData = dataService.getMeta().getEntityMetaData(sourceRepo.getName());
			DefaultAttributeMetaData compoundAttributeMetaData = AnnotatorUtils.getCompoundResultAttribute(annotator,
					entityMetaData);

			Repository targetRepo = addAnnotatorMetadataToRepositories(entityMetaData, createCopy,
					compoundAttributeMetaData);

			if (createCopy)
			{
				// Give current user permissions on the created repo
				permissionSystemService.giveUserEntityPermissions(securityContext, Arrays.asList(targetRepo.getName()));
			}

			Repository crudRepository = iterateOverEntitiesAndAnnotate(sourceRepo, targetRepo, annotator);

			LOG.info("Finished annotating \"" + sourceRepo.getName() + "\" with the " + annotator.getSimpleName()
					+ " annotator (started by \"" + user + "\")");

			return crudRepository;
		});
	}

	/**
	 * Iterates over all the entities within a repository and annotates.
	 */
	private Repository iterateOverEntitiesAndAnnotate(Repository sourceRepo, Repository targetRepo,
			RepositoryAnnotator annotator)
	{
		Iterator<Entity> it = annotator.annotate(sourceRepo);

		List<Entity> batch = new ArrayList<Entity>();
		while (it.hasNext())
		{
			batch.add(it.next());
			if (batch.size() == BATCH_SIZE)
			{
				processBatch(batch, sourceRepo, targetRepo);
				batch.clear();
			}
		}

		if (!batch.isEmpty())
		{
			processBatch(batch, sourceRepo, targetRepo);
		}

		return targetRepo == null ? sourceRepo : targetRepo;
	}

	private void processBatch(List<Entity> batch, Repository sourceRepo, Repository targetRepo)
	{
		if (targetRepo == null)
		{
			sourceRepo.update(batch);
		}
		else
		{
			targetRepo.add(batch);
		}
	}

	/**
	 * Adds a new compound attribute to an existing CrudRepository
	 *
	 * @param entityMetaData
	 *            {@link EntityMetaData} for the existing repository
	 * @param createCopy
	 * @param compoundAttributeMetaData
	 */
	private Repository addAnnotatorMetadataToRepositories(EntityMetaData entityMetaData, boolean createCopy,
			DefaultAttributeMetaData compoundAttributeMetaData)
	{
		if (createCopy)
		{
			DefaultEntityMetaData newEntityMetaData = new DefaultEntityMetaData(RandomStringUtils.randomAlphabetic(30),
					entityMetaData);
			if (newEntityMetaData.getAttribute(compoundAttributeMetaData.getName()) == null)
			{
				newEntityMetaData.addAttributeMetaData(compoundAttributeMetaData);
			}
			newEntityMetaData.setLabel(newRepositoryLabel);

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