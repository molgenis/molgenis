package org.molgenis.data.annotation;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.*;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * This class uses a repository and calls an annotation service to add features and values to an existing repository.
 * </p>
 * 
 * @authors mdehaan,bcharbon
 * 
 * */
public class CrudRepositoryAnnotator
{
	// FIXME unit test this class!
	private static final Logger logger = Logger.getLogger(CrudRepositoryAnnotator.class);

	/**
	 * helper function to use multiple annotators on a repository, if the createRepo boolean is true only a 1 copy of
	 * the set will be made and used for all annotators
	 * 
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
	 * 
	 * @param annotator
	 * @param repo
	 * @param createCopy
	 * 
	 * */
	@Transactional
	public Repository annotate(RepositoryAnnotator annotator, Repository repo, boolean createCopy)
	{
		CrudRepository crudRepository;

		if (!(repo instanceof CrudRepository))
		{
			throw new UnsupportedOperationException("Currently only CrudRepositories can be annotated");
			// TODO: implement createCopy functionality
		}
		else
		{
			crudRepository = (CrudRepository) repo;
		}
		Iterator<Entity> entityIterator = annotator.annotate(repo.iterator());

        addAnnotatorAttributesToRepository(annotator, crudRepository);
        while (entityIterator.hasNext())
		{
			Entity entity = entityIterator.next();
			/**
			 * TODO: implement createCopy functionality if(createCopy){}else{
			 */
			crudRepository.update(entity);
		}

		return crudRepository;
	}

	public void addAnnotatorAttributesToRepository(RepositoryAnnotator annotator, CrudRepository crudRepository)
	{
		addAnnotatorAttributesToRepository(annotator, crudRepository, true);
	}

	public void addAnnotatorAttributesToRepository(RepositoryAnnotator annotator, CrudRepository crudRepository,
			boolean resultToCompound)
	{
        EntityMetaData entityMetadata = crudRepository.getEntityMetaData();
		if (!(entityMetadata instanceof EditableEntityMetaData))
		{
			throw new UnsupportedOperationException("EntityMetadata should be editable to make annotation possible");
		}
		EditableEntityMetaData editableMetadata = (EditableEntityMetaData) entityMetadata;
        Iterator<AttributeMetaData> outputIterator = annotator.getOutputMetaData().getAttributes().iterator();

        if (resultToCompound)
		{
            String attributeName = annotator.getName();
            if ((editableMetadata.getAttribute(annotator.getName()) != null))
			{
				attributeName = annotator.getName() + UUID.randomUUID();
			}
			DefaultAttributeMetaData compoundAttributeMetaData = new DefaultAttributeMetaData(attributeName,
					MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
            compoundAttributeMetaData.setLabel(annotator.getName());
            compoundAttributeMetaData.setAttributesMetaData(annotator.getOutputMetaData().getAtomicAttributes());
            editableMetadata.addAttributeMetaData(compoundAttributeMetaData);
		}
        else{
            while (outputIterator.hasNext())
            {
                editableMetadata.addAttributeMetaData(outputIterator.next());
            }
        }
	}
}