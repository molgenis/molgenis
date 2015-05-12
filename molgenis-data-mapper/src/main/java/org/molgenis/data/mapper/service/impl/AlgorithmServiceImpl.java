package org.molgenis.data.mapper.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.service.AlgorithmService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.MapEntity;
import org.molgenis.js.RhinoConfig;
import org.molgenis.js.ScriptEvaluator;
import org.molgenis.security.core.runas.RunAsSystem;
import org.mozilla.javascript.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Iterables;

public class AlgorithmServiceImpl implements AlgorithmService
{
	private static final Logger LOG = LoggerFactory.getLogger(AlgorithmServiceImpl.class);

	@Autowired
	private DataService dataService;

	@Autowired
	private SemanticSearchService semanticSearchService;

	public AlgorithmServiceImpl()
	{
		new RhinoConfig().init();
	}

	@Override
	@RunAsSystem
	public void autoGenerateAlgorithm(EntityMetaData sourceEntityMetaData, EntityMetaData targetEntityMetaData,
			EntityMapping mapping, AttributeMetaData targetAttribute)
	{
		LOG.debug("createAttributeMappingIfOnlyOneMatch: target= " + targetAttribute.getName());
		Iterable<AttributeMetaData> matches = semanticSearchService.findAttributes(sourceEntityMetaData,
				targetEntityMetaData, targetAttribute);
		if (Iterables.size(matches) == 1)
		{
			AttributeMetaData source = matches.iterator().next();
			AttributeMapping attributeMapping = mapping.addAttributeMapping(targetAttribute.getName());
			String algorithm = "$('" + source.getName() + "').value()";
			attributeMapping.setAlgorithm(algorithm);
			LOG.info("Creating attribute mapping: " + targetAttribute.getName() + " = " + algorithm);
		}
	}

	@Override
	public List<Object> applyAlgorithm(AttributeMetaData targetAttribute, String algorithm, Repository sourceRepository)
	{
		List<Object> derivedValues = new ArrayList<Object>();
		Collection<String> attributeNames = getSourceAttributeNames(algorithm);
		if (!attributeNames.isEmpty())
		{
			for (Entity entity : sourceRepository)
			{
				MapEntity mapEntity = createMapEntity(attributeNames, entity);
				if (!StringUtils.isEmpty(algorithm))
				{
					try
					{
						Object result = ScriptEvaluator
								.eval(algorithm, mapEntity, sourceRepository.getEntityMetaData());

						if (result != null)
						{
							switch (targetAttribute.getDataType().getEnumType())
							{
								case DATE:
								case DATE_TIME:
									derivedValues.add(new Date(Math.round(Context.toNumber(result))));
									break;
								case INT:
									derivedValues.add(Integer.parseInt(Context.toString(result)));
									break;
								case DECIMAL:
									derivedValues.add(Context.toNumber(result));
									break;
								case XREF:
								case CATEGORICAL:
									derivedValues.add(dataService.findOne(targetAttribute.getRefEntity().getName(),
											Context.toString(result)).getIdValue());
									break;
								case MREF:
									throw new UnsupportedOperationException();
								default:
									derivedValues.add(Context.toString(result));
									break;
							}
						}
					}
					catch (RuntimeException e)
					{
						LOG.error("error converting result", e);
					}
				}
			}
		}
		return derivedValues;
	}

	private MapEntity createMapEntity(Collection<String> attributeNames, Entity entity)
	{
		MapEntity mapEntity = new MapEntity();
		for (String attributeName : attributeNames)
		{
			Object value = entity.get(attributeName);
			if (value instanceof Entity)
			{
				value = ((Entity) value).getIdValue();
			}
			mapEntity.set(attributeName, value);
		}
		return mapEntity;
	}

	@Override
	public Object apply(AttributeMapping attributeMapping, Entity sourceEntity, EntityMetaData sourceEntityMetaData)
	{
		String algorithm = attributeMapping.getAlgorithm();
		if (StringUtils.isEmpty(algorithm))
		{
			return null;
		}
		try
		{
			MapEntity entity = createMapEntity(getSourceAttributeNames(attributeMapping.getAlgorithm()), sourceEntity);
			Object value = ScriptEvaluator.eval(algorithm, entity, sourceEntityMetaData);
			return convert(value, attributeMapping.getTargetAttributeMetaData());
		}
		catch (RuntimeException e)
		{
			return null;
		}
	}

	private Object convert(Object value, AttributeMetaData attributeMetaData)
	{
		if (value == null)
		{
			return null;
		}
		Object convertedValue;
		FieldTypeEnum targetDataType = attributeMetaData.getDataType().getEnumType();
		switch (targetDataType)
		{
			case INT:
				convertedValue = Integer.parseInt(Context.toString(value));
				break;
			case DECIMAL:
				convertedValue = Context.toNumber(value);
				break;
			case XREF:
			case CATEGORICAL:
				convertedValue = dataService.findOne(attributeMetaData.getRefEntity().getName(),
						Context.toString(value));
				break;
			case MREF:
				throw new UnsupportedOperationException();
			default:
				convertedValue = Context.toString(value);
				break;
		}
		return convertedValue;
	}

	@Override
	public Collection<String> getSourceAttributeNames(String algorithmScript)
	{
		Collection<String> result = Collections.emptyList();
		if (!StringUtils.isEmpty(algorithmScript))
		{
			result = findMatchesForPattern(algorithmScript, "\\$\\('([^\\$\\(\\)]+)'\\)");
			if (result.isEmpty())
			{
				result = findMatchesForPattern(algorithmScript, "\\$\\(([^\\$\\(\\)]+)\\)");
			}
		}
		return result;
	}

	private static Collection<String> findMatchesForPattern(String algorithmScript, String patternString)
	{
		LinkedHashSet<String> result = new LinkedHashSet<String>();
		Matcher matcher = Pattern.compile(patternString).matcher(algorithmScript);
		while (matcher.find())
		{
			result.add(matcher.group(1));
		}
		return result;
	}

}
