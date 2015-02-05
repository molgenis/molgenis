package org.molgenis.data.annotation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.ElasticsearchRepository;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.security.permission.PermissionSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

public class CrudRepositoryAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(CrudRepositoryAnnotator.class);

	private final MysqlRepositoryCollection mysqlRepositoryCollection;
	private final String newRepositoryLabel;
	private final SearchService searchService;
	private final DataService dataService;
	private final PermissionSystemService permissionSystemService;

	public CrudRepositoryAnnotator(MysqlRepositoryCollection mysqlRepositoryCollection, String newRepositoryName,
			SearchService searchService, DataService dataService, PermissionSystemService permissionSystemService)
	{
		this.mysqlRepositoryCollection = mysqlRepositoryCollection;
		this.newRepositoryLabel = newRepositoryName;
		this.searchService = searchService;
		this.dataService = dataService;
		this.permissionSystemService = permissionSystemService;
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
	 * 
	 *            FIXME: currently the annotators have to have knowledge about mySQL to add the annotated attributes
	 *            FIXME: annotators only work for MySQL and Repositories that update their metadata when an
	 *            Repository.update(Entity) is called. (like the workaround in the elasticSearchRepository)
	 * 
	 * */
	@Transactional
	public Repository annotate(RepositoryAnnotator annotator, Repository sourceRepo, boolean createCopy)
			throws IOException
	{
		if (!(sourceRepo instanceof CrudRepository) && !createCopy)
		{
			throw new UnsupportedOperationException("Currently only CrudRepositories can be annotated");
		}

		if (createCopy) LOG.info("Creating a copy of " + sourceRepo.getName() + " repository, which will be labelled "
				+ newRepositoryLabel + ". A UUID will be generated for the name/identifier");

		if (!createCopy) LOG.info("Annotating " + sourceRepo.getName() + " repository with the "
				+ annotator.getSimpleName() + " annotator");

		EntityMetaData entityMetaData = sourceRepo.getEntityMetaData();
		DefaultAttributeMetaData compoundAttributeMetaData = getCompoundResultAttribute(annotator, entityMetaData);

		CrudRepository targetRepo = addAnnotatorMetadataToRepositories(entityMetaData, createCopy,
				compoundAttributeMetaData);

		CrudRepository crudRepository = iterateOverEntitiesAndAnnotate(sourceRepo, targetRepo, annotator);

		LOG.info("Finished annotating " + sourceRepo.getName() + " with the " + annotator.getSimpleName()
				+ " annotator");

		return crudRepository;
	}

	/**
	 * Iterates over all the entities within a repository and annotates.
	 */
	private CrudRepository iterateOverEntitiesAndAnnotate(Repository sourceRepo, CrudRepository targetRepo,
			RepositoryAnnotator annotator)
	{
		Iterator<Entity> entityIterator = annotator.annotate(sourceRepo.iterator());
		if (targetRepo == null)
		{
			// annotate repository to itself
			CrudRepository annotatedSourceRepository = (CrudRepository) sourceRepo;
			while (entityIterator.hasNext())
			{
				Entity entity = entityIterator.next();
				annotatedSourceRepository.update(entity);
			}
			return annotatedSourceRepository;
		}
		else
		{
			// annotate from source to target repository
			while (entityIterator.hasNext())
			{
				Entity entity = entityIterator.next();
				targetRepo.add(entity);
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
	public CrudRepository addAnnotatorMetadataToRepositories(EntityMetaData entityMetaData, boolean createCopy,
			DefaultAttributeMetaData compoundAttributeMetaData) throws IOException
	{
		CrudRepository newRepository = null;
		if (createCopy)
		{
			newRepository = getOutputRepository(entityMetaData, compoundAttributeMetaData);

			// Give current user permissions on the created repo
			permissionSystemService.giveUserEntityAndMenuPermissions(SecurityContextHolder.getContext(),
					Arrays.asList(newRepository.getEntityMetaData().getName()));
		}
		else
		{
			updateRepositoryMetadata(entityMetaData, compoundAttributeMetaData);
		}
		return newRepository;
	}

	public void updateRepositoryMetadata(EntityMetaData entityMetaData,
			DefaultAttributeMetaData compoundAttributeMetaData)
	{
		// mySQL repository
		if (mysqlRepositoryCollection.getRepositoryByEntityName(entityMetaData.getName()) != null)
		{
			if (entityMetaData.getAttribute(compoundAttributeMetaData.getName()) == null)
			{
				DefaultEntityMetaData newEntityMetaData = new DefaultEntityMetaData(entityMetaData);
				if (newEntityMetaData.getAttribute(compoundAttributeMetaData.getName()) == null)
				{
					newEntityMetaData.addAttributeMetaData(compoundAttributeMetaData);
				}
				mysqlRepositoryCollection.updateSync(newEntityMetaData);
			}
		}
		else
		// ElasticSearch Repository
		{
			if (!(entityMetaData instanceof EditableEntityMetaData))
			{
				throw new UnsupportedOperationException("EntityMetadata should be editable to make annotation possible");
			}
			EditableEntityMetaData editableMetadata = (EditableEntityMetaData) entityMetaData;
			editableMetadata.addAttributeMetaData(compoundAttributeMetaData);
		}
	}

	public CrudRepository getOutputRepository(EntityMetaData entityMetaData,
			DefaultAttributeMetaData compoundAttributeMetaData) throws IOException
	{
		CrudRepository newRepository;
		DefaultEntityMetaData newEntityMetaData = new DefaultEntityMetaData(UUID.randomUUID().toString(),
				entityMetaData);
		if (newEntityMetaData.getAttribute(compoundAttributeMetaData.getName()) == null)
		{
			newEntityMetaData.addAttributeMetaData(compoundAttributeMetaData);
		}
		newEntityMetaData.setLabel(newRepositoryLabel);

		// mySQL source repository
		if (mysqlRepositoryCollection.getRepositoryByEntityName(entityMetaData.getName()) != null)
		{
			newRepository = mysqlRepositoryCollection.add(newEntityMetaData);
		}
		else
		// ElasticSearch source Repository
		{
			newRepository = new ElasticsearchRepository(newEntityMetaData, searchService);
			dataService.addRepository(newRepository);
			searchService.createMappings(newEntityMetaData, true, true, true, true);
		}
		return newRepository;
	}

	public DefaultAttributeMetaData getCompoundResultAttribute(RepositoryAnnotator annotator,
			EntityMetaData entityMetaData)
	{
		DefaultAttributeMetaData compoundAttributeMetaData = new DefaultAttributeMetaData(annotator.getFullName(),
				MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
		compoundAttributeMetaData.setLabel(annotator.getSimpleName());

		Iterator<AttributeMetaData> attributeMetaDataIterator = annotator.getOutputMetaData().getAtomicAttributes()
				.iterator();

		while (attributeMetaDataIterator.hasNext())
		{
			AttributeMetaData currentAmd = attributeMetaDataIterator.next();
			String currentAttributeName = currentAmd.getName();
			if (entityMetaData.getAttribute(currentAttributeName) == null)
			{
				compoundAttributeMetaData.addAttributePart(currentAmd);
			}
		}

		return compoundAttributeMetaData;
	}
}