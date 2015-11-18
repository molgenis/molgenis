package org.molgenis.data.mapper.algorithmgenerator.generator;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.DECIMAL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.INT;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.LONG;

import java.util.Arrays;
import java.util.List;

import javax.measure.converter.ConversionException;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.mapper.service.UnitResolver;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.Objects.requireNonNull;

public class NumericAlgorithmGenerator implements AlgorithmGenerator
{
	private final UnitResolver unitResolver;

	@Autowired
	public NumericAlgorithmGenerator(UnitResolver unitResolver)
	{
		this.unitResolver = requireNonNull(unitResolver);
	}

	public String generate(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes,
			EntityMetaData targetEntityMetaData, EntityMetaData sourceEntityMetaData)
	{
		StringBuilder algorithm = new StringBuilder();

		if (sourceAttributes.size() == 1)
		{
			algorithm.append(generateUnitConversionAlgorithm(targetAttribute, targetEntityMetaData,
					sourceAttributes.get(0), sourceEntityMetaData));
		}
		else if (sourceAttributes.size() > 1)
		{
			algorithm.append("var counter = 0;\nvar SUM=newValue(0);\n");
			for (AttributeMetaData sourceAttribute : sourceAttributes)
			{
				String generate = generate(targetAttribute, Arrays.asList(sourceAttribute), targetEntityMetaData,
						sourceEntityMetaData);
				algorithm.append("if(!$('").append(sourceAttribute.getName()).append("').isNull().value()){\n\t")
						.append("SUM.plus(")
						.append(generate.endsWith(";") ? generate.substring(0, generate.length() - 1) : generate)
						.append(");\n\tcounter++;\n}\n");
			}
			algorithm.append("if(counter !== 0){\n\tSUM.div(counter);\n\tSUM.value();\n}else{\n\tnull;\n}");
		}
		return algorithm.toString();
	}

	public boolean isSuitable(AttributeMetaData targetAttribute, List<AttributeMetaData> sourceAttributes)
	{
		return isNumericDataType(targetAttribute) && (sourceAttributes.stream().allMatch(this::isNumericDataType));
	}

	boolean isNumericDataType(AttributeMetaData attribute)
	{
		FieldTypeEnum enumType = attribute.getDataType().getEnumType();
		return enumType == INT || enumType == LONG || enumType == DECIMAL;
	}

	String generateUnitConversionAlgorithm(AttributeMetaData targetAttribute, EntityMetaData targetEntityMetaData,
			AttributeMetaData sourceAttribute, EntityMetaData sourceEntityMetaData)
	{
		String algorithm = null;

		Unit<? extends Quantity> targetUnit = unitResolver.resolveUnit(targetAttribute, targetEntityMetaData);

		Unit<? extends Quantity> sourceUnit = unitResolver.resolveUnit(sourceAttribute, sourceEntityMetaData);

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
