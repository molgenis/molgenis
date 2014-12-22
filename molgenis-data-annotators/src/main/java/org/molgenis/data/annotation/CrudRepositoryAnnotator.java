package org.molgenis.data.annotation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class CrudRepositoryAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(CrudRepositoryAnnotator.class);

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
	{
		if (!(sourceRepo instanceof CrudRepository) && !createCopy)
		{
			throw new UnsupportedOperationException("Currently only CrudRepositories can be annotated");
		}

		if (createCopy) LOG.info("Creating a copy of " + sourceRepo.getName() + " repository, which will be called "
				+ newRepositoryName);

		if (!createCopy) LOG.info("Annotating " + sourceRepo.getName() + " repository with the " + annotator.getName()
				+ " annotator");

		EntityMetaData entityMetaData = sourceRepo.getEntityMetaData();
		DefaultAttributeMetaData compoundAttributeMetaData = getCompoundResultAttribute(annotator,
				getAttributeName(entityMetaData, annotator), entityMetaData);

		CrudRepository targetRepo = addAnnotatorMetadataToRepositories(entityMetaData, createCopy,
				compoundAttributeMetaData);

		CrudRepository crudRepository = iterateOverEntitiesAndAnnotate(sourceRepo, targetRepo, annotator);

		LOG.info("Finished annotating " + sourceRepo.getName() + " with the " + annotator.getName() + " annotator");

		return crudRepository;
	}

	/**
	 * Iterates over all the entities within a repository and annotates.
	 * 
	 * @param targetRepo
	 * @param targetRepo
	 * @param annotator
	 * @return
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
	 * @param metadata
	 *            {@link EntityMetaData} for the existing repository
	 * @param createCopy
	 * @param compoundAttributeMetaData
	 */
	public CrudRepository addAnnotatorMetadataToRepositories(EntityMetaData metadata, boolean createCopy,
			DefaultAttributeMetaData compoundAttributeMetaData)
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
			if (mysqlRepositoryCollection.getRepositoryByEntityName(metadata.getName()) != null)
			{
				DefaultEntityMetaData newEntityMetaData = new DefaultEntityMetaData(metadata);
				newEntityMetaData.addAttributeMetaData(compoundAttributeMetaData);
				mysqlRepositoryCollection.updateSync(newEntityMetaData);
				return null;
			}
			else
			{
				if (!(metadata instanceof EditableEntityMetaData))
				{
					throw new UnsupportedOperationException(
							"EntityMetadata should be editable to make annotation possible");
				}
				EditableEntityMetaData editableMetadata = (EditableEntityMetaData) metadata;
				editableMetadata.addAttributeMetaData(compoundAttributeMetaData);
				return null;
			}
		}
	}

	public DefaultAttributeMetaData getCompoundResultAttribute(RepositoryAnnotator annotator, String attributeName,
			EntityMetaData entityMetaData)
	{
		DefaultAttributeMetaData compoundAttributeMetaData = new DefaultAttributeMetaData(attributeName,
				MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
		compoundAttributeMetaData.setLabel(annotator.getLabel());

		Iterator<AttributeMetaData> amdIterator = annotator.getOutputMetaData().getAtomicAttributes().iterator();

		while (amdIterator.hasNext())
		{
			AttributeMetaData currentAmd = amdIterator.next();
			String currentAttributeName = currentAmd.getName();
			if (entityMetaData.getAttribute(currentAttributeName) != null)
			{
				String date = new SimpleDateFormat("yyMMddhhmmss").format(new Date());
				String newName = annotator.getName() + "_" + currentAttributeName + "_" + date;
				DefaultAttributeMetaData amd = new DefaultAttributeMetaData(newName, currentAmd);
				amd.setLabel(newName);
				compoundAttributeMetaData.addAttributePart(amd);
			}
			else
			{
				compoundAttributeMetaData.addAttributePart(currentAmd);
			}
		}

		return compoundAttributeMetaData;
	}

	private String getAttributeName(EntityMetaData entityMetaData, RepositoryAnnotator annotator)
	{
		String attributeName = annotator.getName();
		if ((entityMetaData.getAttribute(annotator.getName()) != null))
		{
			throw new RuntimeException("attribute with id: " + annotator.getName() + "already exists");
		}
		return attributeName;
	}

}