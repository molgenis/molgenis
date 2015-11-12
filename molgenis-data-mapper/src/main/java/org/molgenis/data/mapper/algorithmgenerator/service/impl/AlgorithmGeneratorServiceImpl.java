package org.molgenis.data.mapper.algorithmgenerator.service.impl;

import java.util.Arrays;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.mapper.algorithmgenerator.generator.AlgorithmGenerator;
import org.molgenis.data.mapper.algorithmgenerator.generator.NumericAlgorithmGenerator;
import org.molgenis.data.mapper.algorithmgenerator.generator.OneToManyCategoryAlgorithmGenerator;
import org.molgenis.data.mapper.algorithmgenerator.generator.OneToOneCategoryAlgorithmGenerator;
import org.molgenis.data.mapper.algorithmgenerator.service.AlgorithmGeneratorService;
import org.molgenis.data.mapper.service.UnitResolver;
import org.springframework.beans.factory.annotation.Autowired;

public class AlgorithmGeneratorServiceImpl implements AlgorithmGeneratorService
{
	private final List<AlgorithmGenerator> generators;

	@Autowired
	public AlgorithmGeneratorServiceImpl(DataService dataService, UnitResolver unitResolver)
	{
		this.generators = Arrays.asList(new OneToOneCategoryAlgorithmGenerator(dataService),
				new OneToManyCategoryAlgorithmGenerator(dataService), new NumericAlgorithmGenerator(unitResolver));
	}

	public String generate(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes,
			EntityMetaData targetEntityMetaData, EntityMetaData sourceEntityMetaData)
	{
		for (AlgorithmGenerator generator : generators)
		{
			if (generator.isSuitable(targetAttribute, sourceAttributes))
			{
				return generator.generate(targetAttribute, sourceAttributes, targetEntityMetaData,
						sourceEntityMetaData);
			}
		}

		return generateMixTypes(targetAttribute, sourceAttributes, targetEntityMetaData, sourceEntityMetaData);
	}

	String generateMixTypes(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes,
			EntityMetaData targetEntityMetaData, EntityMetaData sourceEntityMetaData)
	{
		StringBuilder stringBuilder = new StringBuilder();
		if (sourceAttributes.size() > 1)
		{
			for (AttributeMetaData sourceAttribute : sourceAttributes)
			{
				stringBuilder.append(generate(targetAttribute, Arrays.asList(sourceAttribute), targetEntityMetaData,
						sourceEntityMetaData));
			}
		}
		return stringBuilder.toString();
	}
}
