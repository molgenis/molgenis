package org.molgenis.data.mapper.repository.impl;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.meta.AttributeMappingMetaData;
import org.molgenis.data.mapper.repository.AttributeMappingRepository;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.DynamicEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.mapper.meta.AttributeMappingMetaData.*;

public class AttributeMappingRepositoryImpl implements AttributeMappingRepository
{
	private final AttributeMappingMetaData attributeMappingMetaData;

	@Autowired
	private IdGenerator idGenerator;

	private final DataService dataService;

	public AttributeMappingRepositoryImpl(DataService dataService, AttributeMappingMetaData attributeMappingMetaData)
	{
		this.dataService = requireNonNull(dataService);
		this.attributeMappingMetaData = requireNonNull(attributeMappingMetaData);
	}

	@Override
	public List<Entity> upsert(Collection<AttributeMapping> attributeMappings)
	{
		List<Entity> result = Lists.newArrayList();
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
			attributeMapping.setIdentifier(idGenerator.generateId());
			result = toAttributeMappingEntity(attributeMapping);
			dataService.add(attributeMappingMetaData.getName(), result);
		}
		else
		{
			result = toAttributeMappingEntity(attributeMapping);
			dataService.update(attributeMappingMetaData.getName(), result);
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

	@Override
	public List<Attribute> retrieveAttributeMetaDatasFromAlgorithm(String algorithm,
			EntityMetaData sourceEntityMetaData)
	{
		List<Attribute> sourceAttributes = Lists.newArrayList();

		Pattern pattern = Pattern.compile("\\$\\('([^']+)'\\)");
		Matcher matcher = pattern.matcher(algorithm);

		while (matcher.find())
		{
			Attribute attribute = sourceEntityMetaData.getAttribute(matcher.group(1));
			if (!sourceAttributes.contains(attribute))
			{
				sourceAttributes.add(attribute);
			}
		}

		return sourceAttributes;
	}

	private AttributeMapping toAttributeMapping(Entity attributeMappingEntity, EntityMetaData sourceEntityMetaData,
			EntityMetaData targetEntityMetaData)
	{
		String identifier = attributeMappingEntity.getString(IDENTIFIER);
		String targetAtributeName = attributeMappingEntity.getString(TARGETATTRIBUTEMETADATA);
		Attribute targetAttribute = targetEntityMetaData.getAttribute(targetAtributeName);
		String algorithm = attributeMappingEntity.getString(ALGORITHM);
		String algorithmState = attributeMappingEntity.getString(ALGORITHMSTATE);
		List<Attribute> sourceAttributes = retrieveAttributeMetaDatasFromAlgorithm(algorithm,
				sourceEntityMetaData);

		return new AttributeMapping(identifier, targetAttribute, algorithm, sourceAttributes,
				algorithmState);
	}

	private Entity toAttributeMappingEntity(AttributeMapping attributeMapping)
	{
		Entity attributeMappingEntity = new DynamicEntity(attributeMappingMetaData);
		attributeMappingEntity.set(IDENTIFIER, attributeMapping.getIdentifier());
		attributeMappingEntity.set(TARGETATTRIBUTEMETADATA,
				attributeMapping.getTargetAttribute() != null ? attributeMapping.getTargetAttribute()
						.getName() : null);
		attributeMappingEntity.set(ALGORITHM, attributeMapping.getAlgorithm());
		attributeMappingEntity.set(SOURCEATTRIBUTEMETADATAS,
				attributeMapping.getSourceAttributes().stream().map(Attribute::getName)
						.collect(Collectors.joining(",")));
		attributeMappingEntity.set(ALGORITHMSTATE, attributeMapping.getAlgorithmState().toString());
		return attributeMappingEntity;
	}

}
