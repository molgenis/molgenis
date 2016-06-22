package org.molgenis.data.annotation;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.AttributeMetaDataFactory;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataFactory;
import org.molgenis.data.vcf.VcfAttributes;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.molgenis.MolgenisFieldTypes.COMPOUND;

@Component
public class CrudRepositoryAnnotator
{
	private static final int BATCH_SIZE = 1000;

	private final DataService dataService;

	@Autowired
	public CrudRepositoryAnnotator(DataService dataService)
	{
		this.dataService = dataService;
	}

	@Autowired
	public EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	public AttributeMetaDataFactory attributeMetaDataFactory;

	@Autowired
	public VcfAttributes vcfAttributes;

	/**
	 * @param annotators
	 * @param repo
	 */
	public void annotate(List<RepositoryAnnotator> annotators, Repository<Entity> repo) throws IOException
	{
		for (RepositoryAnnotator annotator : annotators)
		{
			repo = annotate(annotator, repo);
		}
	}

	/**
	 * @param annotator
	 * @param repository
	 */
	public Repository<Entity> annotate(RepositoryAnnotator annotator, Repository<Entity> repository) throws IOException
	{
		if (!repository.getCapabilities().contains(RepositoryCapability.WRITABLE))
		{
			throw new UnsupportedOperationException("Currently only writable repositories can be annotated");
		}
		try
		{
			EntityMetaData entityMetaData = dataService.getMeta().getEntityMetaData(repository.getName());
			List<AttributeMetaData> attributeMetaDatas = annotator.getOutputAttributes();

			RunAsSystemProxy.runAsSystem(
					() -> addAnnotatorMetadataToRepositories(entityMetaData, annotator.getSimpleName(),
							attributeMetaDatas));

			Repository<Entity> crudRepository = iterateOverEntitiesAndAnnotate(
					dataService.getRepository(repository.getName()), annotator);
			return crudRepository;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Iterates over all the entities within a repository and annotates.
	 */
	private Repository<Entity> iterateOverEntitiesAndAnnotate(Repository<Entity> repository,
			RepositoryAnnotator annotator)
	{
		Iterator<Entity> it = annotator.annotate(repository);

		List<Entity> batch = new ArrayList<>();
		while (it.hasNext())
		{
			batch.add(it.next());
			if (batch.size() == BATCH_SIZE)
			{
				processBatch(batch, repository);
				batch.clear();
			}
		}

		if (!batch.isEmpty())
		{
			processBatch(batch, repository);
		}

		return repository;
	}

	private void processBatch(List<Entity> batch, Repository<Entity> repository)
	{
		repository.update(batch.stream());
	}

	/**
	 * Adds a new compound attribute to an existing CrudRepository
	 *
	 * @param entityMetaData {@link EntityMetaData} for the existing repository
	 * @param annotatorName
	 */
	private void addAnnotatorMetadataToRepositories(EntityMetaData entityMetaData, String annotatorName,
			List<AttributeMetaData> attributeMetaDatas)
	{
		//FIXME: add the attributes in the compound once the generating of id's in the factory is implemented
		//currently this would nullpointer
		AttributeMetaData compoundAttributeMetaData = attributeMetaDataFactory.create()
				.setName("MOLGENIS_" + annotatorName).setDataType(COMPOUND);
		if (entityMetaData.getAttribute("MOLGENIS_" + annotatorName) == null)
		{
			for (AttributeMetaData part : attributeMetaDatas)
			{
				//compoundAttributeMetaData.addAttributePart(part);
				if(entityMetaData.getAttribute(part.getName()) == null)
					entityMetaData.addAttribute(part);
			}
			//entityMetaData.addAttribute(compoundAttributeMetaData);
			dataService.getMeta().updateEntityMeta(entityMetaData);
		}
	}

}