package org.molgenis.semanticmapper.repository.impl;

import com.google.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping;
import org.molgenis.semanticmapper.meta.AttributeMappingMetaData;
import org.molgenis.semanticmapper.repository.AttributeMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.molgenis.semanticmapper.meta.AttributeMappingMetaData.*;

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
			dataService.add(attributeMappingMetaData.getId(), result);
		}
		else
		{
			result = toAttributeMappingEntity(attributeMapping);
			dataService.update(attributeMappingMetaData.getId(), result);
		}
		return result;
	}

	@Override
	public List<AttributeMapping> getAttributeMappings(List<Entity> attributeMappingEntities,
			EntityType sourceEntityType, EntityType targetEntityType)
	{
		return Lists.transform(attributeMappingEntities,
				attributeMappingEntity -> toAttributeMapping(attributeMappingEntity, sourceEntityType,
						targetEntityType));

	}

	@Override
	public List<Attribute> retrieveAttributesFromAlgorithm(String algorithm, EntityType sourceEntityType)
	{
		List<Attribute> sourceAttributes = Lists.newArrayList();

		Pattern pattern = Pattern.compile("\\$\\('([^']+)'\\)");
		Matcher matcher = pattern.matcher(algorithm);

		while (matcher.find())
		{
			String sourceAttribute = matcher.group(1).split("\\.")[0];
			Attribute attribute = sourceEntityType.getAttribute(sourceAttribute);
			if (!sourceAttributes.contains(attribute))
			{
				sourceAttributes.add(attribute);
			}
		}

		return sourceAttributes;
	}

	private AttributeMapping toAttributeMapping(Entity attributeMappingEntity, EntityType sourceEntityType,
			EntityType targetEntityType)
	{
		String identifier = attributeMappingEntity.getString(IDENTIFIER);
		String targetAttributeName = attributeMappingEntity.getString(TARGET_ATTRIBUTE);
		Attribute targetAttribute = targetEntityType.getAttribute(targetAttributeName);
		String algorithm = attributeMappingEntity.getString(ALGORITHM);
		String algorithmState = attributeMappingEntity.getString(ALGORITHM_STATE);
		List<Attribute> sourceAttributes = retrieveAttributesFromAlgorithm(algorithm, sourceEntityType);

		return new AttributeMapping(identifier, targetAttributeName, targetAttribute, algorithm, sourceAttributes,
				algorithmState);
	}

	private Entity toAttributeMappingEntity(AttributeMapping attributeMapping)
	{
		Entity attributeMappingEntity = new DynamicEntity(attributeMappingMetaData);
		attributeMappingEntity.set(IDENTIFIER, attributeMapping.getIdentifier());
		attributeMappingEntity.set(TARGET_ATTRIBUTE, attributeMapping.getTargetAttributeName());
		attributeMappingEntity.set(ALGORITHM, attributeMapping.getAlgorithm());
		attributeMappingEntity.set(SOURCE_ATTRIBUTES, attributeMapping.getSourceAttributes()
																	  .stream()
																	  .map(Attribute::getName)
																	  .collect(Collectors.joining(",")));
		attributeMappingEntity.set(ALGORITHM_STATE, attributeMapping.getAlgorithmState().toString());
		return attributeMappingEntity;
	}

}
