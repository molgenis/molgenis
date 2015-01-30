package org.molgenis.data.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.mapping.model.AttributeMapping;
import org.molgenis.data.mapping.model.EntityMapping;
import org.molgenis.data.support.MapEntity;
import org.molgenis.js.ScriptEvaluator;
import org.mozilla.javascript.Context;

public class AlgorithmServiceImpl implements AlgorithmService
{
	@Override
	public List<Object> applyAlgorithm(AttributeMetaData targetAttribute, Iterable<AttributeMetaData> sourceAttributes,
			String algorithm, Repository sourceRepository)
	{
		List<Object> derivedValues = new ArrayList<Object>();
		FieldTypeEnum dataType = targetAttribute.getDataType().getEnumType();
		for (Entity entity : sourceRepository)
		{
			MapEntity mapEntity = new MapEntity();
			for (AttributeMetaData attributeMetaData : sourceAttributes)
			{
				mapEntity.set(attributeMetaData.getName(), entity.get(attributeMetaData.getName()));
			}
			if (!StringUtils.isEmpty(algorithm))
			{
				Object result = ScriptEvaluator.eval(algorithm, mapEntity);

				if (result != null)
				{
					switch (dataType)
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
		}
		return derivedValues;
	}

	@Override
	public List<Object> applyAlgorithm(AttributeMapping attributeMapping, Repository sourceRepo)
	{
		return null;
	}

	@Override
	public List<Entity> applyAlgorithms(EntityMapping entityMappings)
	{
		return null;
	}

	public static List<String> extractFeatureName(String algorithmScript)
	{
		List<String> featureNames = new ArrayList<String>();
		if (!StringUtils.isEmpty(algorithmScript))
		{
			Pattern pattern = Pattern.compile("\\$\\('([^\\$\\(\\)]*)'\\)");
			Matcher matcher = pattern.matcher(algorithmScript);
			while (matcher.find())
			{
				if (!featureNames.contains(matcher.group(1))) featureNames.add(matcher.group(1));
			}
			if (featureNames.size() > 0) return featureNames;

			pattern = Pattern.compile("\\$\\(([^\\$\\(\\)]*)\\)");
			matcher = pattern.matcher(algorithmScript);
			while (matcher.find())
			{
				if (!featureNames.contains(matcher.group(1))) featureNames.add(matcher.group(1));
			}
		}
		return featureNames;
	}
}
