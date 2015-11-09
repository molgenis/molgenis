package org.molgenis.data.mapper.algorithmgenerator.service.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.mapper.algorithmgenerator.generator.AlgorithmGenerator;
import org.molgenis.data.mapper.algorithmgenerator.generator.OneToManyCategoryAlgorithmGenerator;
import org.molgenis.data.mapper.algorithmgenerator.generator.OneToOneCategoryAlgorithmGenerator;
import org.molgenis.data.mapper.algorithmgenerator.service.AlgorithmGeneratorService;

public class AlgorithmGeneratorServiceImpl implements AlgorithmGeneratorService
{
	private final List<AlgorithmGenerator> generators;

	public AlgorithmGeneratorServiceImpl(DataService dataService)
	{
		this.generators = Arrays.asList(new OneToOneCategoryAlgorithmGenerator(dataService),
				new OneToManyCategoryAlgorithmGenerator(dataService));
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
		return StringUtils.EMPTY;
	}
}
