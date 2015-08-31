package org.molgenis.data.mapper.categorymapper.convertor;

import java.util.OptionalDouble;
import java.util.Set;

import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.molgenis.data.mapper.categorymapper.utils.DurationUnitConversionUtil;

public class NumberAmountConvertor extends AmountConvertor
{
	public boolean matchCriteria(String description)
	{
		return true;
	}

	Amount<?> getInternalAmount(String description)
	{
		Set<Double> extractNumbers = DurationUnitConversionUtil.extractNumbers(description);
		Unit<?> unit = DurationUnitConversionUtil.findDurationUnit(description);
		Double average = average(extractNumbers);
		if (average != null && unit != null)
		{
			return Amount.valueOf(average, unit);
		}
		return null;
	}

	Double average(Set<Double> values)
	{
		OptionalDouble average = values.stream().mapToDouble(Double::doubleValue).average();
		return average.isPresent() ? average.getAsDouble() : null;
	}
}
