package org.molgenis.data.annotation;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.*;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.transaction.annotation.Transactional;

public class CrudRepositoryAnnotator
{
    public CrudRepositoryAnnotator(MysqlRepositoryCollection mysqlRepositoryCollection) {
        this.mysqlRepositoryCollection = mysqlRepositoryCollection;
    }

    private static final Logger logger = Logger.getLogger(CrudRepositoryAnnotator.class);
    private final MysqlRepositoryCollection mysqlRepositoryCollection;

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
     * FIXME: currently the annotators have to have knowledge about mySQL to add the annotated attributes
     * FIXME: annotators only work for MySQL and Repositories that update their metadata when an Repository.update(Entity) is called. (like the workaround in the elasticSearchRepository)
     * FIXME: "createCopy" functionality should be implemented
	 * */
	@Transactional
	public Repository annotate(RepositoryAnnotator annotator, Repository repo, boolean createCopy)
	{
        EntityMetaData entityMetaData = repo.getEntityMetaData();
        CrudRepository crudRepository;
        if (!(repo instanceof CrudRepository))
        {
            throw new UnsupportedOperationException("Currently only CrudRepositories can be annotated");
        }
        else
        {
            crudRepository = (CrudRepository) repo;
        }
        if(mysqlRepositoryCollection.getRepositoryByEntityName(entityMetaData.getName())!=null) {
            addAnnotatorMetadata(annotator, entityMetaData);
        }else{
            addAnnotatorAttributesToRepository(annotator, crudRepository);
        }
        Iterator<Entity> entityIterator = annotator.annotate(repo.iterator());
        while (entityIterator.hasNext())
		{
			Entity entity = entityIterator.next();

            try {
                crudRepository.update(entity);
            }catch(Exception e){
                e.printStackTrace();
            }
		}

		return crudRepository;
	}

    public void addAnnotatorMetadata(RepositoryAnnotator annotator, EntityMetaData metadata) {
        EditableEntityMetaData entityMetadata = (EditableEntityMetaData)metadata;
        String attributeName = annotator.getName();
        if ((entityMetadata.getAttribute(annotator.getName()) != null))
        {
            attributeName = annotator.getName() + UUID.randomUUID();
        }
        DefaultAttributeMetaData compoundAttributeMetaData = getComoundResultAttribute(annotator, attributeName);
        DefaultEntityMetaData newEntityMetaData = new DefaultEntityMetaData(entityMetadata);
        newEntityMetaData.addAttributeMetaData(compoundAttributeMetaData);
        mysqlRepositoryCollection.update(newEntityMetaData);
    }

    public void addAnnotatorAttributesToRepository(RepositoryAnnotator annotator, CrudRepository crudRepository)
	{
        EntityMetaData entityMetadata = crudRepository.getEntityMetaData();
		if (!(entityMetadata instanceof EditableEntityMetaData))
		{
			throw new UnsupportedOperationException("EntityMetadata should be editable to make annotation possible");
		}
		EditableEntityMetaData editableMetadata = (EditableEntityMetaData) entityMetadata;
        Iterator<AttributeMetaData> outputIterator = annotator.getOutputMetaData().getAttributes().iterator();

        String attributeName = annotator.getName();
        if ((editableMetadata.getAttribute(annotator.getName()) != null))
        {
            attributeName = annotator.getName() + UUID.randomUUID();
		}
        DefaultAttributeMetaData compoundAttributeMetaData = getComoundResultAttribute(annotator, attributeName);
        editableMetadata.addAttributeMetaData(compoundAttributeMetaData);
	}

    public DefaultAttributeMetaData getComoundResultAttribute(RepositoryAnnotator annotator, String attributeName) {
        DefaultAttributeMetaData compoundAttributeMetaData = new DefaultAttributeMetaData(attributeName,
                MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
        compoundAttributeMetaData.setLabel(annotator.getName());
        compoundAttributeMetaData.setAttributesMetaData(annotator.getOutputMetaData().getAtomicAttributes());
        return compoundAttributeMetaData;
    }
}