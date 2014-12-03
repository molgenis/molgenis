package org.molgenis.data.annotation;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.Repository;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.transaction.annotation.Transactional;

public class CrudRepositoryAnnotator
{
	private static final Logger logger = Logger.getLogger(CrudRepositoryAnnotator.class);
	private final MysqlRepositoryCollection mysqlRepositoryCollection;
	private String newRepositoryName;

	public CrudRepositoryAnnotator(MysqlRepositoryCollection mysqlRepositoryCollection, String newRepositoryName)
	{
		this.mysqlRepositoryCollection = mysqlRepositoryCollection;
		this.newRepositoryName = newRepositoryName;
	}

	/**
	 * @param annotators
	 * @param repo
	 * @param createCopy
	 */
	public void annotate(List<RepositoryAnnotator> annotators, Repository repo, boolean createCopy)
	{
		for (RepositoryAnnotator annotator : annotators)
		{
			repo = annotate(annotator, repo, createCopy);
			createCopy = false;
		}
	}

	/**
	 * @param annotator
	 * @param repo
	 * @param createCopy
	 * 
	 *            FIXME: currently the annotators have to have knowledge about mySQL to add the annotated attributes
	 *            FIXME: "createCopy" functionality should be implemented FIXME: annotators only work for MySQL and
	 *            Repositories that update their metadata when an Repository.update(Entity) is called. (like the
	 *            workaround in the elasticSearchRepository)
	 * 
	 * */
	@Transactional
	public Repository annotate(RepositoryAnnotator annotator, Repository sourceRepo, boolean createCopy)
	{
		if (!(sourceRepo instanceof CrudRepository) && !createCopy)
		{
			throw new UnsupportedOperationException("Currently only CrudRepositories can be annotated");
		}
		
		logger.info("Starting annotator " + annotator.getName());
		if(createCopy) logger.info("Creating a copy of " + sourceRepo.getName() + " repository");
		if(!createCopy) logger.info("Annotating " + sourceRepo.getName() + " repository");

		EntityMetaData entityMetaData = sourceRepo.getEntityMetaData();
		DefaultAttributeMetaData compoundAttributeMetaData = getCompoundResultAttribute(annotator, getAttributeName(entityMetaData, annotator));

		CrudRepository targetRepo = addAnnotatorMetadataToRepositories(entityMetaData, createCopy,
				compoundAttributeMetaData);

		CrudRepository crudRepository = iterateOverEntitiesAndAnnotate(createCopy, sourceRepo, targetRepo, annotator);
		
		logger.info("Finished annotating");
		
		return crudRepository;
	}

	/**
	 * Iterates over all the entities within a repository and annotates.
	 * 
	 * @param createCopy
	 * @param targetRepo
	 * @param crudRepository
	 * @param annotator
	 * @return
	 */
	private CrudRepository iterateOverEntitiesAndAnnotate(boolean createCopy, Repository sourceRepo,
			CrudRepository targetRepo, RepositoryAnnotator annotator)
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
	 * {@link #mysqlRepositoryCollection}.
	 * 
	 * @param annotator
	 *            the {@link RepositoryAnnotator} that is used to determine the name of the compound attribute to add
	 * @param metadata
	 *            {@link EntityMetaData} for the existing repository
	 * @param createCopy
	 * @param compoundAttributeMetaData
	 */
	public CrudRepository addAnnotatorMetadataToRepositories(EntityMetaData metadata, boolean createCopy,
			DefaultAttributeMetaData compoundAttributeMetaData)
	{
		if (mysqlRepositoryCollection.getRepositoryByEntityName(metadata.getName()) != null)
		{
			if (createCopy)
			{
				DefaultEntityMetaData newEntityMetaData = new DefaultEntityMetaData(newRepositoryName, metadata);
				newEntityMetaData.addAttributeMetaData(compoundAttributeMetaData);
				newEntityMetaData.setLabel(newRepositoryName);
				return mysqlRepositoryCollection.add(newEntityMetaData);
			}
			else
			{
				DefaultEntityMetaData newEntityMetaData = new DefaultEntityMetaData(metadata);
				newEntityMetaData.addAttributeMetaData(compoundAttributeMetaData);
				mysqlRepositoryCollection.update(newEntityMetaData);
				return null;
			}
		}
		else
		{
			if (!(metadata instanceof EditableEntityMetaData))
			{
				throw new UnsupportedOperationException("EntityMetadata should be editable to make annotation possible");
			}
			EditableEntityMetaData editableMetadata = (EditableEntityMetaData) metadata;
			editableMetadata.addAttributeMetaData(compoundAttributeMetaData);

			return null;
		}
	}

	public DefaultAttributeMetaData getCompoundResultAttribute(RepositoryAnnotator annotator, String attributeName)
	{
		DefaultAttributeMetaData compoundAttributeMetaData = new DefaultAttributeMetaData(attributeName,
				MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
		compoundAttributeMetaData.setLabel(annotator.getName());

		// FIXME #2187
		// change the outputMetaData attribute name so we dont get duplicate column name errors from mysql
		compoundAttributeMetaData.setAttributesMetaData(annotator.getOutputMetaData().getAtomicAttributes());
		return compoundAttributeMetaData;
	}

	private String getAttributeName(EntityMetaData entityMetaData, RepositoryAnnotator annotator)
	{
		String attributeName = annotator.getName();
		if ((entityMetaData.getAttribute(annotator.getName()) != null))
		{
			// TODO make it something more human readible then random ID
			attributeName = annotator.getName() + UUID.randomUUID();
		}
		return attributeName;
	}

}