package org.molgenis.data.repository.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.mapping.model.AttributeMapping;
import org.molgenis.data.meta.AttributeMappingMetaData;
import org.molgenis.data.meta.EntityMappingMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.repository.AttributeMappingRepository;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.IdGenerator;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class AttributeMappingRepositoryImpl implements AttributeMappingRepository
{
	@Autowired
	private MetaDataService metaDataService;

	@Autowired
	private IdGenerator idGenerator;

	public static final EntityMetaData META_DATA = new AttributeMappingMetaData();
	private CrudRepository repository;

	public AttributeMappingRepositoryImpl(CrudRepository repository)
	{
		this.repository = repository;
	}

	@Override
	public List<Entity> upsert(Collection<AttributeMapping> attributeMappings)
	{
		List<Entity> result = new ArrayList<Entity>();
		for (AttributeMapping attributeMapping : attributeMappings)
		{
			result.add(upsert(attributeMapping));
		}
		return result;
	}

	private Entity upsert(AttributeMapping attributeMapping)
	{
		Entity result;
		if (attributeMapping.getIdentifier() == null)
		{
			attributeMapping.setIdentifier(idGenerator.generateId().toString());
			result = toAttributeMappingEntity(attributeMapping);
			repository.add(result);
		}
		else
		{
			result = toAttributeMappingEntity(attributeMapping);
			repository.add(result);
		}
		return result;
	}

	@Override
	public List<AttributeMapping> getAttributeMappings(List<Entity> attributeMappingEntities,
			EntityMetaData sourceEntityMetaData, EntityMetaData targetEntityMetaData)
	{
		return Lists.transform(attributeMappingEntities, new Function<Entity, AttributeMapping>()
		{
			@Override
			public AttributeMapping apply(Entity attributeMappingEntity)
			{
				return toAttributeMapping(attributeMappingEntity, sourceEntityMetaData, targetEntityMetaData);
			}
		});

	}

	private AttributeMapping toAttributeMapping(Entity attributeMappingEntity, EntityMetaData sourceEntityMetaData,
			EntityMetaData targetEntityMetaData)
	{
		String identifier = attributeMappingEntity.getString(EntityMappingMetaData.IDENTIFIER);
		String sourceAttributeName = attributeMappingEntity.getString(AttributeMappingMetaData.SOURCEATTRIBUTEMETADATA);
		AttributeMetaData sourceAttributeMetaData = sourceEntityMetaData.getAttribute(sourceAttributeName);
		String targetAtributeName = attributeMappingEntity.getString(AttributeMappingMetaData.TARGETATTRIBUTEMETADATA);
		AttributeMetaData targetAttributeMetaData = targetEntityMetaData.getAttribute(targetAtributeName);
		String algorithm = AttributeMappingMetaData.ALGORITHM;

		return new AttributeMapping(identifier, sourceAttributeMetaData, targetAttributeMetaData, algorithm);
	}

	private Entity toAttributeMappingEntity(AttributeMapping attributeMapping)
	{
		Entity attributeMappingEntity = new MapEntity();
		attributeMappingEntity.set(AttributeMappingMetaData.IDENTIFIER, attributeMapping.getIdentifier());
		attributeMappingEntity.set(AttributeMappingMetaData.SOURCEATTRIBUTEMETADATA, attributeMapping
				.getSourceAttributeMetaData() != null ? attributeMapping.getSourceAttributeMetaData().getName() : null);
		attributeMappingEntity.set(AttributeMappingMetaData.TARGETATTRIBUTEMETADATA, attributeMapping
				.getTargetAttributeMetaData() != null ? attributeMapping.getTargetAttributeMetaData().getName() : null);
		attributeMappingEntity.set(AttributeMappingMetaData.ALGORITHM, attributeMapping.getAlgorithm());
		return attributeMappingEntity;
	}
}
