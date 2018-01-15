package org.molgenis.semanticmapper.utils;

import org.apache.commons.lang3.StringUtils;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MagmaUnitConverter
{
	public String convertUnit(Unit<? extends Quantity> standardUnit, Unit<? extends Quantity> unit)
	{
		if (standardUnit != null && unit != null)
		{
			StringBuilder conversionScript = new StringBuilder();

			for (String standardUnitName : findCompositeUnitNames(standardUnit.toString()))
			{
				for (String customeUnitName : findCompositeUnitNames(unit.toString()))
				{
					Unit<?> unit1 = Unit.valueOf(standardUnitName);

					Unit<?> unit2 = Unit.valueOf(customeUnitName);

					if (unit1 != null && unit2 != null && unit1.isCompatible(unit2) && !unit1.equals(unit2))
					{
						Amount<?> value2 = Amount.valueOf(1, unit2);
						Amount<?> value1 = value2.to(unit1);
						double estimatedValue1 = value1.getEstimatedValue();
						double estimatedValue2 = value2.getEstimatedValue();

						if (estimatedValue1 > estimatedValue2)
						{
							conversionScript.append(".times(")
											.append(value1.divide(value2).getEstimatedValue())
											.append(")");
						}
						else
						{
							conversionScript.append(".div(")
											.append(value2.divide(value1).getEstimatedValue())
											.append(")");
						}
					}

					if (conversionScript.length() > 0)
					{
						return conversionScript.toString();
					}
				}
			}
		}

		return StringUtils.EMPTY;
	}

	Set<String> findCompositeUnitNames(String unitName)
	{
		Set<String> newUnitNames = new HashSet<>();

		if (StringUtils.isNotBlank(unitName))
		{
			newUnitNames.add(unitName);

			if (unitName.contains("/"))
			{
				newUnitNames.addAll(Arrays.asList(unitName.split("/"))
										  .stream()
										  .map(UnitHelper::superscriptToNumber)
										  .map(unit -> unit.replaceAll("\\d+", ""))
										  .collect(Collectors.toSet()));
			}
		}
		return newUnitNames;
	}
}
