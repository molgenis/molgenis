package org.molgenis.semanticmapper.algorithmgenerator.generator;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.semanticmapper.service.UnitResolver;

import javax.measure.converter.ConversionException;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.*;

public class NumericAlgorithmGenerator implements AlgorithmGenerator
{
	private final UnitResolver unitResolver;

	public NumericAlgorithmGenerator(UnitResolver unitResolver)
	{
		this.unitResolver = requireNonNull(unitResolver);
	}

	public String generate(Attribute targetAttribute, List<Attribute> sourceAttributes, EntityType targetEntityType,
			EntityType sourceEntityType)
	{
		StringBuilder algorithm = new StringBuilder();

		if (sourceAttributes.size() == 1)
		{
			algorithm.append(generateUnitConversionAlgorithm(targetAttribute, targetEntityType, sourceAttributes.get(0),
					sourceEntityType));
		}
		else if (sourceAttributes.size() > 1)
		{
			algorithm.append("var counter = 0;\nvar SUM=newValue(0);\n");
			for (Attribute sourceAttribute : sourceAttributes)
			{
				String generate = generate(targetAttribute, Arrays.asList(sourceAttribute), targetEntityType,
						sourceEntityType);
				algorithm.append("if(!$('")
						 .append(sourceAttribute.getName())
						 .append("').isNull().value()){\n\t")
						 .append("SUM.plus(")
						 .append(generate.endsWith(";") ? generate.substring(0, generate.length() - 1) : generate)
						 .append(");\n\tcounter++;\n}\n");
			}
			algorithm.append("if(counter !== 0){\n\tSUM.div(counter);\n\tSUM.value();\n}else{\n\tnull;\n}");
		}
		return algorithm.toString();
	}

	public boolean isSuitable(Attribute targetAttribute, List<Attribute> sourceAttributes)
	{
		return isNumericDataType(targetAttribute) && (sourceAttributes.stream().allMatch(this::isNumericDataType));
	}

	boolean isNumericDataType(Attribute attribute)
	{
		AttributeType enumType = attribute.getDataType();
		return enumType == INT || enumType == LONG || enumType == DECIMAL;
	}

	String generateUnitConversionAlgorithm(Attribute targetAttribute, EntityType targetEntityType,
			Attribute sourceAttribute, EntityType sourceEntityType)
	{
		String algorithm = null;

		Unit<? extends Quantity> targetUnit = unitResolver.resolveUnit(targetAttribute, targetEntityType);

		Unit<? extends Quantity> sourceUnit = unitResolver.resolveUnit(sourceAttribute, sourceEntityType);

		if (sourceUnit != null)
		{
			if (targetUnit != null && !sourceUnit.equals(targetUnit))
			{
				// if units are convertible, create convert algorithm
				UnitConverter unitConverter;
				try
				{
					unitConverter = sourceUnit.getConverterTo(targetUnit);
				}
				catch (ConversionException e)
				{
					unitConverter = null;
					// algorithm sets source unit and assigns source value to target
					algorithm = String.format("$('%s').unit('%s').value();", sourceAttribute.getName(),
							sourceUnit.toString());
				}

				if (unitConverter != null)
				{
					// algorithm sets source unit and assigns value converted to target unit to target
					algorithm = String.format("$('%s').unit('%s').toUnit('%s').value();", sourceAttribute.getName(),
							sourceUnit.toString(), targetUnit.toString());
				}
			}
			else
			{
				// algorithm sets source unit and assigns source value to target
				algorithm = String.format("$('%s').unit('%s').value();", sourceAttribute.getName(),
						sourceUnit.toString());
			}
		}

		if (algorithm == null)
		{
			// algorithm assigns source value to target
			algorithm = String.format("$('%s').value();", sourceAttribute.getName());
		}

		return algorithm;
	}
}
