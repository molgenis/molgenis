package org.molgenis.data.mapper.categorymapper;

import java.util.OptionalDouble;
import java.util.Set;

import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import com.google.common.collect.Sets;

public class FrequencyCategoryMapper
{
	private static final Set<AmountConvertor> CONVERTORS = Sets.newHashSet(new DailyAmountConvertor());

	public Amount<?> convertCategory(Amount<?> amount1, Unit<?> unit, double valueToConvert)
	{
		if (amount1.getUnit().isCompatible(unit))
		{
			Amount<?> amount2 = amount1.to(unit);
			return amount2.times(valueToConvert);
		}
		return Amount.valueOf(valueToConvert, unit);
	}

	public Amount<?> convertDescriptionToAmount(String description)
	{
		String cleanedDescription = DurationUnitConversionUtil.convertWordToNumber(description);

		for (AmountConvertor convertor : CONVERTORS)
		{
			if (convertor.matchCriteria(cleanedDescription)) return convertor.getAmount(cleanedDescription);
		}

		Set<Double> extractNumbers = DurationUnitConversionUtil.extractNumbers(cleanedDescription);
		Unit<?> unit = DurationUnitConversionUtil.findDurationUnit(cleanedDescription);
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
