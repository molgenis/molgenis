package org.molgenis.semanticmapper.algorithmgenerator.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.semanticmapper.algorithmgenerator.bean.GeneratedAlgorithm;
import org.molgenis.semanticmapper.algorithmgenerator.generator.AlgorithmGenerator;
import org.molgenis.semanticmapper.algorithmgenerator.generator.NumericAlgorithmGenerator;
import org.molgenis.semanticmapper.algorithmgenerator.generator.OneToManyCategoryAlgorithmGenerator;
import org.molgenis.semanticmapper.algorithmgenerator.generator.OneToOneCategoryAlgorithmGenerator;
import org.molgenis.semanticmapper.algorithmgenerator.service.AlgorithmGeneratorService;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping.AlgorithmState;
import org.molgenis.semanticmapper.service.UnitResolver;
import org.molgenis.semanticmapper.service.impl.AlgorithmTemplate;
import org.molgenis.semanticmapper.service.impl.AlgorithmTemplateService;
import org.molgenis.semanticmapper.utils.AlgorithmGeneratorHelper;
import org.molgenis.semanticmapper.utils.MagmaUnitConverter;
import org.molgenis.semanticsearch.explain.bean.ExplainedAttribute;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.molgenis.semanticmapper.mapping.model.AttributeMapping.AlgorithmState.GENERATED_HIGH;
import static org.molgenis.semanticmapper.mapping.model.AttributeMapping.AlgorithmState.GENERATED_LOW;

public class AlgorithmGeneratorServiceImpl implements AlgorithmGeneratorService
{
	private final List<AlgorithmGenerator> generators;
	private final AlgorithmTemplateService algorithmTemplateService;
	private final UnitResolver unitResolver;
	private final MagmaUnitConverter magmaUnitConverter = new MagmaUnitConverter();

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
		if (!sourceAttributes.isEmpty())
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
				stringBuilder.append(
						generate(targetAttribute, Arrays.asList(sourceAttribute), targetEntityType, sourceEntityType));
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
			AlgorithmTemplate algorithmTemplate = algorithmTemplateService.find(sourceAttributes)
																		  .findFirst()
																		  .orElse(null);
			if (algorithmTemplate != null)
			{
				algorithm = algorithmTemplate.render();
				mappedSourceAttributes = AlgorithmGeneratorHelper.extractSourceAttributesFromAlgorithm(algorithm,
						sourceEntityType);
				algorithm = convertUnitForTemplateAlgorithm(algorithm, targetAttribute, targetEntityType,
						mappedSourceAttributes, sourceEntityType);
				algorithmState = GENERATED_HIGH;
			}
			else
			{
				Entry<Attribute, ExplainedAttribute> firstEntry = sourceAttributes.entrySet()
																				  .stream()
																				  .findFirst()
																				  .get();
				Attribute sourceAttribute = firstEntry.getKey();
				algorithm = generate(targetAttribute, Arrays.asList(sourceAttribute), targetEntityType,
						sourceEntityType);
				mappedSourceAttributes = AlgorithmGeneratorHelper.extractSourceAttributesFromAlgorithm(algorithm,
						sourceEntityType);
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
