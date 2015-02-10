package org.molgenis.data.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.collect.Iterables;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.mapping.model.AttributeMapping;
import org.molgenis.data.support.MapEntity;
import org.molgenis.js.RhinoConfig;
import org.molgenis.js.ScriptEvaluator;
import org.mozilla.javascript.Context;

public class AlgorithmServiceImpl implements AlgorithmService
{
	public AlgorithmServiceImpl()
	{
		new RhinoConfig().init();
	}

	@Override
	public List<Object> applyAlgorithm(AttributeMetaData targetAttribute, Iterable<AttributeMetaData> sourceAttributes,
			String algorithm, Repository sourceRepository)
	{
		List<Object> derivedValues = new ArrayList<Object>();
		if (Iterables.size(sourceAttributes) > 0)
		{
			for (Entity entity : sourceRepository)
			{
				MapEntity mapEntity = new MapEntity();
				for (AttributeMetaData attributeMetaData : sourceAttributes)
				{
					mapEntity.set(attributeMetaData.getName(), entity.get(attributeMetaData.getName()));
				}
				if (!StringUtils.isEmpty(algorithm))
				{
					try
					{
						Object result = ScriptEvaluator.eval(algorithm, mapEntity);

						if (result != null)
						{
							switch (targetAttribute.getDataType().getEnumType())
							{
								case INT:
									derivedValues.add(Integer.parseInt(Context.toString(result)));
									break;
								case DECIMAL:
									derivedValues.add(Context.toNumber(result));
									break;
								default:
									derivedValues.add(Context.toString(result));
									break;
							}
						}
					}
					catch (RuntimeException ignored)
					{
					}
				}
			}
		}
		return derivedValues;
	}

	@Override
	public Object apply(AttributeMapping attributeMapping, Entity sourceEntity)
	{
		String algorithm = attributeMapping.getAlgorithm();
		if (StringUtils.isEmpty(algorithm))
		{
			return null;
		}
		try
		{
			Object value = ScriptEvaluator.eval(algorithm, sourceEntity);
			return convert(value, attributeMapping.getTargetAttributeMetaData().getDataType().getEnumType());
		}
		catch (RuntimeException e)
		{
			return null;
		}
	}

	private static Object convert(Object value, FieldTypeEnum targetDataType)
	{
		if (value == null)
		{
			return null;
		}
		Object convertedValue;
		switch (targetDataType)
		{
			case INT:
				convertedValue = Integer.parseInt(Context.toString(value));
				break;
			case DECIMAL:
				convertedValue = Context.toNumber(value);
				break;
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
