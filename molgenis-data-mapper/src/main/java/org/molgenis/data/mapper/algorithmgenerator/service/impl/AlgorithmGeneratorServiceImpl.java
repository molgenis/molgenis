package org.molgenis.data.mapper.algorithmgenerator.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.mapper.algorithmgenerator.bean.GeneratedAlgorithm;
import org.molgenis.data.mapper.algorithmgenerator.generator.AlgorithmGenerator;
import org.molgenis.data.mapper.algorithmgenerator.generator.NumericAlgorithmGenerator;
import org.molgenis.data.mapper.algorithmgenerator.generator.OneToManyCategoryAlgorithmGenerator;
import org.molgenis.data.mapper.algorithmgenerator.generator.OneToOneCategoryAlgorithmGenerator;
import org.molgenis.data.mapper.algorithmgenerator.service.AlgorithmGeneratorService;
import org.molgenis.data.mapper.mapping.model.AttributeMapping.AlgorithmState;
import org.molgenis.data.mapper.service.UnitResolver;
import org.molgenis.data.mapper.service.impl.AlgorithmTemplate;
import org.molgenis.data.mapper.service.impl.AlgorithmTemplateService;
import org.molgenis.data.mapper.utils.AlgorithmGeneratorHelper;
import org.molgenis.data.mapper.utils.MagmaUnitConverter;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedAttribute;
import org.springframework.beans.factory.annotation.Autowired;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.mapper.mapping.model.AttributeMapping.AlgorithmState.GENERATED_HIGH;
import static org.molgenis.data.mapper.mapping.model.AttributeMapping.AlgorithmState.GENERATED_LOW;

public class AlgorithmGeneratorServiceImpl implements AlgorithmGeneratorService
{
	private final List<AlgorithmGenerator> generators;
	private final AlgorithmTemplateService algorithmTemplateService;
	private final UnitResolver unitResolver;
	private final MagmaUnitConverter magmaUnitConverter = new MagmaUnitConverter();

	@Autowired
	public AlgorithmGeneratorServiceImpl(DataService dataService, UnitResolver unitResolver,
			AlgorithmTemplateService algorithmTemplateService)
	{
		this.algorithmTemplateService = requireNonNull(algorithmTemplateService);
		this.unitResolver = requireNonNull(unitResolver);
		this.generators = Arrays.asList(new OneToOneCategoryAlgorithmGenerator(dataService),
				new OneToManyCategoryAlgorithmGenerator(dataService), new NumericAlgorithmGenerator(unitResolver));
	}

	@Override
	public String generate(Attribute targetAttribute, List<Attribute> sourceAttributes, EntityType targetEntityType,
			EntityType sourceEntityType)
	{
		if (sourceAttributes.size() > 0)
		{
			for (AlgorithmGenerator generator : generators)
			{
				if (generator.isSuitable(targetAttribute, sourceAttributes))
				{
					return generator.generate(targetAttribute, sourceAttributes, targetEntityType, sourceEntityType);
				}
			}
			return generateMixedTypes(targetAttribute, sourceAttributes, targetEntityType, sourceEntityType);
		}

		return StringUtils.EMPTY;
	}

	String generateMixedTypes(Attribute targetAttribute, List<Attribute> sourceAttributes, EntityType targetEntityType,
			EntityType sourceEntityType)
	{
		StringBuilder stringBuilder = new StringBuilder();

		if (sourceAttributes.size() == 1)
		{
			stringBuilder.append(String.format("$('%s').value();", sourceAttributes.get(0).getName()));
		}
		else if (sourceAttributes.size() > 1)
		{
			for (Attribute sourceAttribute : sourceAttributes)
			{
				stringBuilder.append(generate(targetAttribute, Arrays.asList(sourceAttribute), targetEntityType,
						sourceEntityType));
			}
		}

		return stringBuilder.toString();
	}

	@Override
	public GeneratedAlgorithm generate(Attribute targetAttribute, Map<Attribute, ExplainedAttribute> sourceAttributes,
			EntityType targetEntityType, EntityType sourceEntityType)
	{
		String algorithm = StringUtils.EMPTY;
		AlgorithmState algorithmState = null;
		Set<Attribute> mappedSourceAttributes = null;

		if (sourceAttributes.size() > 0)
		{
			AlgorithmTemplate algorithmTemplate = algorithmTemplateService.find(sourceAttributes).findFirst()
					.orElse(null);
			if (algorithmTemplate != null)
			{
				algorithm = algorithmTemplate.render();
				mappedSourceAttributes = AlgorithmGeneratorHelper
						.extractSourceAttributesFromAlgorithm(algorithm, sourceEntityType);
				algorithm = convertUnitForTemplateAlgorithm(algorithm, targetAttribute, targetEntityType,
						mappedSourceAttributes, sourceEntityType);
				algorithmState = GENERATED_HIGH;
			}
			else
			{
				Entry<Attribute, ExplainedAttribute> firstEntry = sourceAttributes.entrySet().stream().findFirst()
						.get();
				Attribute sourceAttribute = firstEntry.getKey();
				algorithm = generate(targetAttribute, Arrays.asList(sourceAttribute), targetEntityType,
						sourceEntityType);
				mappedSourceAttributes = AlgorithmGeneratorHelper
						.extractSourceAttributesFromAlgorithm(algorithm, sourceEntityType);
				algorithmState = firstEntry.getValue().isHighQuality() ? GENERATED_HIGH : GENERATED_LOW;
			}
		}

		return GeneratedAlgorithm.create(algorithm, mappedSourceAttributes, algorithmState);
	}

	String convertUnitForTemplateAlgorithm(String algorithm, Attribute targetAttribute, EntityType targetEntityType,
			Set<Attribute> sourceAttributes, EntityType sourceEntityType)
	{
		Unit<? extends Quantity> targetUnit = unitResolver.resolveUnit(targetAttribute, targetEntityType);

		for (Attribute sourceAttribute : sourceAttributes)
		{
			Unit<? extends Quantity> sourceUnit = unitResolver.resolveUnit(sourceAttribute, sourceEntityType);

			String convertUnit = magmaUnitConverter.convertUnit(targetUnit, sourceUnit);

			if (StringUtils.isNotBlank(convertUnit))
			{
				String attrMagamSyntax = String.format("$('%s')", sourceAttribute.getName());
				String unitConvertedMagamSyntax = convertUnit.startsWith(".") ?
						attrMagamSyntax + convertUnit : attrMagamSyntax + "." + convertUnit;
				algorithm = StringUtils.replace(algorithm, attrMagamSyntax, unitConvertedMagamSyntax);
			}
		}

		return algorithm;
	}
}
