package org.molgenis.data.merge;

import org.molgenis.MolgenisFieldTypes;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.Repository;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.AbstractEntity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by charbonb on 01/09/14.
 */
@Component
public class RepositoryMerger
{

	private final static String ID = "ID";
	private DataService dataService;
	private String idField;

	@Autowired
	public RepositoryMerger(DataService dataService)
	{
		this.dataService = dataService;
	}

	/**
	 * Create a new merged repository Metadata is merged based on the common attributes (those remain at root level) All
	 * non-common level attributes are organised in 1 compound attribute per repository Data of all repositories is
	 * merged based on the common columns
	 * 
	 * @param repositoryList
	 *            list of repositories to be merged
	 * @param commonAttributes
	 *            list of common attributes, these columns are use to 'join'/'merge' on
	 * @param mergedRepository
	 *            the resulting repository default of 1000 for param: batchSize number of records after which the result
	 *            is added or updated in the repository
	 * @return mergedRepository ElasticSearchRepository containing the merged data
	 */
	public CrudRepository merge(List<Repository> repositoryList, List<AttributeMetaData> commonAttributes,
			CrudRepository mergedRepository, String idField)
	{
		return merge(repositoryList, commonAttributes, mergedRepository, idField, 1000);
	}

	/**
	 * Create a new merged repository Metadata is merged based on the common attributes (those remain at root level) All
	 * non-common level attributes are organised in 1 compound attribute per repository Data of all repositories is
	 * merged based on the common columns
	 * 
	 * @param repositoryList
	 *            list of repositories to be merged
	 * @param commonAttributes
	 *            list of common attributes, these columns are use to 'join'/'merge' on
	 * @param mergedRepository
	 *            the resulting repository
	 * @param batchSize
	 *            number of records after which the result is added or updated in the repository
	 * @return mergedRepository ElasticSearchRepository containing the merged data
	 */
	public CrudRepository merge(List<Repository> repositoryList, List<AttributeMetaData> commonAttributes,
			CrudRepository mergedRepository, String idField, int batchSize)
	{
		this.idField = idField;
		dataService.addRepository(mergedRepository);
		mergeData(repositoryList, (CrudRepository) dataService.getRepositoryByEntityName(mergedRepository.getName()),
				commonAttributes, batchSize);

		return mergedRepository;
	}

	/**
	 * Merge the data of all repositories based on the common columns
	 */
	private void mergeData(List<Repository> originalRepositoriesList, CrudRepository resultRepository,
			List<AttributeMetaData> commonAttributes, int batchSize)
	{
		for (Repository repository : originalRepositoriesList)
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
				for (AttributeMetaData attributeMetaData : entity.getEntityMetaData().getAtomicAttributes())
				{
					if (!containsIgnoreCase(attributeMetaData.getName(), commonAttributes))
					{
						mergedEntity.set(getMergedAttributeName(repository, attributeMetaData.getName()),
								entity.get(attributeMetaData.getName())); // word er hier een map ingedrukt?
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
					resultRepository.add(addedEntities);
					addedEntities = new ArrayList<Entity>();
				}
				if (updatedEntities.size() == batchSize)
				{
					resultRepository.update(updatedEntities);
					updatedEntities = new ArrayList<Entity>();
				}
			}
			// write remaining entities to repository
			resultRepository.add(addedEntities);
			resultRepository.update(updatedEntities);
		}
	}

	/**
	 * create a new entity based on the merged entity metadata
	 */
	private AbstractEntity createMergedEntity(List<AttributeMetaData> commonAttributes, Entity entity)
	{
		AbstractEntity mergedEntity;
		mergedEntity = new MapEntity(new HashMap<String, Object>());
		if (idField == null || !entity.getEntityMetaData().getAttribute(idField).isUnique())
		{
			mergedEntity.set(ID, UUID.randomUUID().toString());// "CHROM"+entity.get("#CHROM")+"POS"+entity.get("POS"));
		}
		else
		{
			mergedEntity.set(ID, entity.getString(idField));
		}
		for (AttributeMetaData attributeMetaData : commonAttributes)
		{
			mergedEntity.set(attributeMetaData.getName(), entity.get(attributeMetaData.getName()));
		}
		return mergedEntity;
	}

	/**
	 * check if an entity for the common attributes already exists and if so, return it
	 */
	private Entity getMergedEntity(CrudRepository crudRepository, List<AttributeMetaData> commonAttributes,
			Entity entity)
	{
		Query findMergedEntityQuery = new QueryImpl();
		for (AttributeMetaData attributeMetaData : commonAttributes)
		{
			if (!findMergedEntityQuery.getRules().isEmpty()) findMergedEntityQuery = findMergedEntityQuery.and();
			findMergedEntityQuery = findMergedEntityQuery.eq(attributeMetaData.getName(),
					entity.get(attributeMetaData.getName()));
		}

		Entity result = (Entity) crudRepository.findOne(findMergedEntityQuery);
		return result;
	}

	/**
	 * Create new EntityMetaData with the common attributes at root level, and all other columns in a compound attribute
	 * per original repository
	 */
	public EntityMetaData mergeMetaData(List<Repository> repositoryList, List<AttributeMetaData> commonAttributes,
			String outRepositoryName)
	{
		DefaultEntityMetaData mergedMetaData = new DefaultEntityMetaData(outRepositoryName);
		DefaultAttributeMetaData idAttribute = new DefaultAttributeMetaData(ID, MolgenisFieldTypes.FieldTypeEnum.STRING);
		idAttribute.setIdAttribute(true);
		idAttribute.setVisible(false);
		mergedMetaData.addAttributeMetaData(idAttribute);
		mergedMetaData.setIdAttribute(ID);
		for (AttributeMetaData attributeMetaData : commonAttributes)
		{
			mergedMetaData.addAttributeMetaData(attributeMetaData);
		}
		for (Repository repository : repositoryList)
		{
			mergeRepositoryMetaData(commonAttributes, mergedMetaData, repository);
		}
		return mergedMetaData;
	}

	/**
	 * Add a compound attribute for a repository containing all "non-common" attributes
	 */
	private void mergeRepositoryMetaData(List<AttributeMetaData> commonAttributes,
			DefaultEntityMetaData mergedMetaData, Repository repository)
	{
		EntityMetaData originalRepositoryMetaData = repository.getEntityMetaData();
		DefaultAttributeMetaData repositoryCompoundAttribute = new DefaultAttributeMetaData(repository.getName(),
				MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
		List<AttributeMetaData> attributeParts = new ArrayList<AttributeMetaData>();
		for (AttributeMetaData originalRepositoryAttributeMetaData : originalRepositoryMetaData.getAttributes())
		{
			if (!containsIgnoreCase(originalRepositoryAttributeMetaData.getName(), commonAttributes)
					&& !originalRepositoryAttributeMetaData.getName().equalsIgnoreCase(ID))
			{
				DefaultAttributeMetaData attributePartMetaData = copyAndRename(originalRepositoryAttributeMetaData,
						getMergedAttributeName(repository, originalRepositoryAttributeMetaData.getName()),
						getMergedAttributeLabel(repository, originalRepositoryAttributeMetaData.getLabel()));
				// new DefaultAttributeMetaData(getMergedAttributeName(
				// repository, originalRepositoryAttributeMetaData.getName()), originalRepositoryAttributeMetaData
				// .getDataType().getEnumType());
				// attributePartMetaData.setRefEntity(originalRepositoryAttributeMetaData.getRefEntity());
				// attributePartMetaData.setLabel(getMergedAttributeLabel(repository,
				// originalRepositoryAttributeMetaData.getLabel()));
				if (originalRepositoryAttributeMetaData.getDataType().getEnumType()
						.equals(MolgenisFieldTypes.FieldTypeEnum.COMPOUND))
				{
					addCompoundAttributeParts(repository, originalRepositoryAttributeMetaData, attributePartMetaData);
				}
				attributeParts.add(attributePartMetaData);
			}
		}
		repositoryCompoundAttribute.setAttributesMetaData(attributeParts);
		mergedMetaData.addAttributeMetaData(repositoryCompoundAttribute);
	}

	/**
	 * Recursively add all the attributes in an compound attribute
	 */
	private void addCompoundAttributeParts(Repository repository,
			AttributeMetaData originalRepositoryAttributeMetaData, DefaultAttributeMetaData attributePartMetaData)
	{
		List<AttributeMetaData> subAttributeParts = new ArrayList<AttributeMetaData>();
		for (AttributeMetaData originalRepositorySubAttributeMetaData : originalRepositoryAttributeMetaData
				.getAttributeParts())
		{
			DefaultAttributeMetaData subAttributePartMetaData = new DefaultAttributeMetaData(getMergedAttributeName(
					repository, originalRepositorySubAttributeMetaData.getName()),
					originalRepositorySubAttributeMetaData.getDataType().getEnumType());
			subAttributePartMetaData.setLabel(getMergedAttributeLabel(repository,
					originalRepositorySubAttributeMetaData.getLabel()));
			if (subAttributePartMetaData.getDataType().getEnumType().equals(MolgenisFieldTypes.FieldTypeEnum.COMPOUND))
			{
				addCompoundAttributeParts(repository, originalRepositorySubAttributeMetaData, subAttributePartMetaData);
			}
			subAttributeParts.add(subAttributePartMetaData);
		}
		attributePartMetaData.setAttributesMetaData(subAttributeParts);
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
	private String getMergedAttributeName(Repository repository, String attributeName)
	{
		return repository.getName() + "_" + attributeName;
	}

	/**
	 * Create a label for an attribute based on the attribute label in the original repository and the original
	 * repository name itself.
	 */
	private String getMergedAttributeLabel(Repository repository, String attributeLabel)
	{
		return attributeLabel + "(" + repository.getName() + ")";
	}

	private DefaultAttributeMetaData copyAndRename(AttributeMetaData attributeMetaData, String name, String label)
	{
		DefaultAttributeMetaData result = new DefaultAttributeMetaData(name, attributeMetaData.getDataType()
				.getEnumType());
		result.setDescription(attributeMetaData.getDescription());
		result.setNillable(attributeMetaData.isNillable());
		result.setReadOnly(attributeMetaData.isReadonly());
		result.setDefaultValue(attributeMetaData.getDefaultValue());
		result.setLookupAttribute(attributeMetaData.isLookupAttribute());
		result.setRefEntity(attributeMetaData.getRefEntity());
		result.setLabel(label);
		result.setVisible(attributeMetaData.isVisible());
		result.setUnique(attributeMetaData.isUnique());
		result.setAggregateable(attributeMetaData.isAggregateable());
		result.setRange(attributeMetaData.getRange());
		return result;
	}
}
