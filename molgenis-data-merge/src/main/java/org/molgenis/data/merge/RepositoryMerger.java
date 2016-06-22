package org.molgenis.data.merge;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.util.ApplicationContextProvider.getApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by charbonb on 01/09/14.
 */
@Component
public class RepositoryMerger
{
	private final static String ID = "ID";
	private final DataService dataService;
	private final AttributeMetaDataFactory attrMetaFactory;

	@Autowired
	public RepositoryMerger(DataService dataService, AttributeMetaDataFactory attrMetaFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.attrMetaFactory = requireNonNull(attrMetaFactory);
	}

	/**
	 * Create a new merged repository Metadata is merged based on the common attributes (those remain at root level) All
	 * non-common level attributes are organised in 1 compound attribute per repository Data of all repositories is
	 * merged based on the common columns
	 *
	 * @param repositoryList   list of repositories to be merged
	 * @param commonAttributes list of common attributes, these columns are use to 'join'/'merge' on
	 * @param mergedRepository the resulting repository default of 1000 for param: batchSize number of records after which the result
	 *                         is added or updated in the repository
	 * @return mergedRepository ElasticSearchRepository containing the merged data
	 */
	public Repository<Entity> merge(List<Repository<Entity>> repositoryList, List<AttributeMetaData> commonAttributes,
			Repository<Entity> mergedRepository)
	{
		return merge(repositoryList, commonAttributes, mergedRepository, 1000);
	}

	/**
	 * Create a new merged repository Metadata is merged based on the common attributes (those remain at root level) All
	 * non-common level attributes are organised in 1 compound attribute per repository Data of all repositories is
	 * merged based on the common columns
	 *
	 * @param repositoryList   list of repositories to be merged
	 * @param commonAttributes list of common attributes, these columns are use to 'join'/'merge' on
	 * @param mergedRepository the resulting repository
	 * @param batchSize        number of records after which the result is added or updated in the repository
	 * @return mergedRepository ElasticSearchRepository containing the merged data
	 */
	public Repository<Entity> merge(List<Repository<Entity>> repositoryList, List<AttributeMetaData> commonAttributes,
			Repository<Entity> mergedRepository, int batchSize)
	{
		mergeData(repositoryList, dataService.getRepository(mergedRepository.getName()), commonAttributes, batchSize);

		return mergedRepository;
	}

	/**
	 * Merge the data of all repositories based on the common columns
	 */
	private void mergeData(List<Repository<Entity>> originalRepositoriesList, Repository<Entity> resultRepository,
			List<AttributeMetaData> commonAttributes, int batchSize)
	{
		for (Repository<Entity> repository : originalRepositoriesList)
		{
			List<Entity> addedEntities = new ArrayList<Entity>();
			List<Entity> updatedEntities = new ArrayList<Entity>();
			for (Entity entity : repository)
			{
				boolean newEntity = false;

				Entity mergedEntity = getMergedEntity(resultRepository, commonAttributes, entity);
				// if no entity for all the common columns exists, create a new one, containing these fields
				if (mergedEntity == null)
				{
					newEntity = true;
					mergedEntity = createMergedEntity(commonAttributes, entity);
				}
				// add all data for non common fields
				EntityMetaData entityMeta = entity.getEntityMetaData();
				for (AttributeMetaData attr : entityMeta.getAtomicAttributes())
				{
					if ((!attr.equals(entityMeta.getIdAttribute()) || attr.isVisible()) && !containsIgnoreCase(
							attr.getName(), commonAttributes))
					{
						mergedEntity
								.set(getMergedAttributeName(repository, attr.getName()), entity.get(attr.getName()));
					}
				}
				if (newEntity)
				{
					addedEntities.add(mergedEntity);
				}
				else
				{
					updatedEntities.add(mergedEntity);
				}

				// write to repository after every 1000 entities
				if (addedEntities.size() == batchSize)
				{
					resultRepository.add(addedEntities.stream());
					addedEntities = new ArrayList<Entity>();
				}
				if (updatedEntities.size() == batchSize)
				{
					resultRepository.update(updatedEntities.stream());
					updatedEntities = new ArrayList<Entity>();
				}
			}
			// write remaining entities to repository
			resultRepository.add(addedEntities.stream());
			resultRepository.update(updatedEntities.stream());
		}
	}

	/**
	 * create a new entity based on the merged entity metadata
	 */
	private Entity createMergedEntity(List<AttributeMetaData> commonAttributes, Entity entity)
	{
		Entity mergedEntity = new DynamicEntity(null); // FIXME pass entity meta data instead of null
		mergedEntity.set(ID, UUID.randomUUID().toString());

		for (AttributeMetaData attributeMetaData : commonAttributes)
		{
			mergedEntity.set(attributeMetaData.getName(), entity.get(attributeMetaData.getName()));
		}
		return mergedEntity;
	}

	/**
	 * check if an entity for the common attributes already exists and if so, return it
	 */
	private Entity getMergedEntity(Repository<Entity> repository, List<AttributeMetaData> commonAttributes,
			Entity entity)
	{
		Query<Entity> findMergedEntityQuery = new QueryImpl<Entity>();
		for (AttributeMetaData attributeMetaData : commonAttributes)
		{
			if (!findMergedEntityQuery.getRules().isEmpty()) findMergedEntityQuery = findMergedEntityQuery.and();
			findMergedEntityQuery = findMergedEntityQuery
					.eq(attributeMetaData.getName(), entity.get(attributeMetaData.getName()));
		}

		Entity result = repository.findOne(findMergedEntityQuery);
		return result;
	}

	/**
	 * Create new EntityMetaData with the common attributes at root level, and all other columns in a compound attribute
	 * per original repository
	 */
	public EntityMetaData mergeMetaData(List<Repository<Entity>> repositoryList, List<AttributeMetaData> commonAttrs,
			AttributeMetaData commonIdAttr, String outRepositoryName)
	{
		EntityMetaDataFactory entityMetaFactory = getApplicationContext().getBean(EntityMetaDataFactory.class);
		AttributeMetaDataFactory attrMetaFactory = getApplicationContext().getBean(AttributeMetaDataFactory.class);

		EntityMetaData mergedMetaData = entityMetaFactory.create().setSimpleName(outRepositoryName);
		mergedMetaData.addAttribute(attrMetaFactory.create().setName(ID).setVisible(false), ROLE_ID);

		for (AttributeMetaData commonAttr : commonAttrs)
		{
			if (commonAttr.equals(commonIdAttr))
			{
				// Ignore hidden id attributes
				if (commonAttr.isVisible())
				{
					// We added a new ID, save old attribute but do not use it as id
					commonAttr = AttributeMetaData.newInstance(commonAttr);
				}
			}
			else
			{
				mergedMetaData.addAttribute(commonAttr);
			}
		}

		for (Repository<Entity> repository : repositoryList)
		{
			mergeRepositoryMetaData(commonAttrs, mergedMetaData, repository);
		}
		return mergedMetaData;
	}

	/**
	 * Add a compound attribute for a repository containing all "non-common" attributes
	 */
	private void mergeRepositoryMetaData(List<AttributeMetaData> commonAttributes, EntityMetaData mergedMetaData,
			Repository<Entity> repository)
	{
		EntityMetaData originalRepositoryMetaData = repository.getEntityMetaData();
		AttributeMetaData repositoryCompoundAttribute = attrMetaFactory.create().setName(repository.getName())
				.setDataType(MolgenisFieldTypes.COMPOUND);
		List<AttributeMetaData> attributeParts = new ArrayList<>();
		for (AttributeMetaData originalRepositoryAttr : originalRepositoryMetaData.getAttributes())
		{
			if (!containsIgnoreCase(originalRepositoryAttr.getName(), commonAttributes))
			{
				if (!originalRepositoryAttr.equals(originalRepositoryMetaData.getIdAttribute())
						|| originalRepositoryAttr.isVisible())
				{
					AttributeMetaData attributePartMetaData = copyAndRename(originalRepositoryAttr,
							getMergedAttributeName(repository, originalRepositoryAttr.getName()),
							getMergedAttributeLabel(repository, originalRepositoryAttr.getLabel()));
					if (originalRepositoryAttr.getDataType().getEnumType()
							.equals(MolgenisFieldTypes.FieldTypeEnum.COMPOUND))
					{
						addCompoundAttributeParts(repository, originalRepositoryAttr, attributePartMetaData);
					}
					attributeParts.add(attributePartMetaData);
				}
			}
		}
		repositoryCompoundAttribute.setAttributeParts(attributeParts);
		mergedMetaData.addAttribute(repositoryCompoundAttribute);
	}

	/**
	 * Recursively add all the attributes in an compound attribute
	 */
	private void addCompoundAttributeParts(Repository<Entity> repository,
			AttributeMetaData originalRepositoryAttributeMetaData, AttributeMetaData attributePartMetaData)
	{
		List<AttributeMetaData> subAttributeParts = new ArrayList<AttributeMetaData>();
		for (AttributeMetaData originalRepositorySubAttributeMetaData : originalRepositoryAttributeMetaData
				.getAttributeParts())
		{
			AttributeMetaData subAttributePartMetaData = copyAndRename(originalRepositorySubAttributeMetaData,
					getMergedAttributeName(repository, originalRepositorySubAttributeMetaData.getName()),
					getMergedAttributeLabel(repository, originalRepositoryAttributeMetaData.getLabel()));
			subAttributePartMetaData
					.setLabel(getMergedAttributeLabel(repository, originalRepositorySubAttributeMetaData.getLabel()));
			if (subAttributePartMetaData.getDataType().getEnumType().equals(MolgenisFieldTypes.FieldTypeEnum.COMPOUND))
			{
				addCompoundAttributeParts(repository, originalRepositorySubAttributeMetaData, subAttributePartMetaData);
			}
			subAttributeParts.add(subAttributePartMetaData);
		}
		attributePartMetaData.setAttributeParts(subAttributeParts);
	}

	/**
	 * Check if an specific attributename is present in a list of AttributeMetadata
	 */
	private boolean containsIgnoreCase(String input, List<AttributeMetaData> list)
	{
		for (AttributeMetaData attributeMetaData : list)
		{
			if (input.equalsIgnoreCase(attributeMetaData.getName())) return true;
		}
		return false;
	}

	/**
	 * Create a name for an attribute based on the attribute name in the original repository and the original repository
	 * name itself.
	 */
	private String getMergedAttributeName(Repository<Entity> repository, String attributeName)
	{
		return repository.getName() + "_" + attributeName;
	}

	/**
	 * Create a label for an attribute based on the attribute label in the original repository and the original
	 * repository name itself.
	 */
	private String getMergedAttributeLabel(Repository<Entity> repository, String attributeLabel)
	{
		return attributeLabel + "(" + repository.getName() + ")";
	}

	private AttributeMetaData copyAndRename(AttributeMetaData attributeMetaData, String name, String label)
	{
		AttributeMetaData result = attrMetaFactory.create().setName(name).setDataType(attributeMetaData.getDataType());
		result.setDescription(attributeMetaData.getDescription());
		result.setNillable(true);// We got a problem if a attr is required in one entitymeta and missing in another
		result.setReadOnly(false);
		result.setDefaultValue(attributeMetaData.getDefaultValue());
		result.setRefEntity(attributeMetaData.getRefEntity());
		result.setLabel(label);
		result.setVisible(attributeMetaData.isVisible());
		result.setUnique(attributeMetaData.isUnique());
		result.setAggregatable(attributeMetaData.isAggregatable());
		result.setRange(attributeMetaData.getRange());

		return result;
	}
}
