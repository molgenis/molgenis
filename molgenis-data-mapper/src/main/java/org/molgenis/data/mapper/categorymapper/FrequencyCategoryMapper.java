package org.molgenis.data.mapper.categorymapper;

import java.util.HashSet;
import java.util.List;

import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.molgenis.data.mapper.categorymapper.convertor.AmountConvertor;
import org.molgenis.data.mapper.categorymapper.convertor.DailyAmountConvertor;
import org.molgenis.data.mapper.categorymapper.convertor.NumberAmountConvertor;
import org.molgenis.data.mapper.categorymapper.convertor.SeveralTimesConvertor;

import com.google.common.collect.Lists;

public class FrequencyCategoryMapper
{
	private static final List<AmountConvertor> CONVERTORS = Lists.newArrayList(new DailyAmountConvertor(),
			new SeveralTimesConvertor(), new NumberAmountConvertor());

	public Double convert(Amount<?> sourceAmount, Amount<?> targetAmount)
	{
		Unit<?> standardUnit = targetAmount.getUnit();
		if (sourceAmount.getUnit().isCompatible(standardUnit))
		{
			if (isMaxValueUndetermined(sourceAmount))
			{
				double maxValue = sourceAmount.getMaximumValue();
				if (!sourceAmount.getUnit().equals(standardUnit))
				{
					maxValue = standardUnit.getConverterTo(sourceAmount.getUnit()).convert((double) 1);
				}
				sourceAmount = Amount.rangeOf(sourceAmount.getMinimumValue(), maxValue, sourceAmount.getUnit());
			}
			Amount<?> convertedSourceAmount = sourceAmount.to(standardUnit);
			return convertFactor(convertedSourceAmount, targetAmount);
		}
		return null;
	}

	public Amount<?> convertDescriptionToAmount(String description)
	{
		String cleanedDescription = DurationUnitConversionUtil.convertWordToNumber(description);

		for (AmountConvertor convertor : CONVERTORS)
		{
			if (convertor.matchCriteria(cleanedDescription))
			{
				return convertor.getAmount(cleanedDescription);
			}
		}
		return null;
	}

	double convertFactor(Amount<?> convertedSourceAmount, Amount<?> targetAmount)
	{
		double lowerBoundDiff = Math.abs(targetAmount.getMinimumValue() - convertedSourceAmount.getMinimumValue());
		double upperBoundDiff = Math.abs(targetAmount.getMaximumValue() - convertedSourceAmount.getMaximumValue());
		return (lowerBoundDiff + upperBoundDiff) / 2;
	}

	boolean isMaxValueUndetermined(Amount<?> amount1)
	{
		return DurationUnitConversionUtil.isAmountRanged(amount1) && amount1.getMaximumValue() == Double.MAX_VALUE;
	}

	public Amount<?> findBestAmount(Amount<?> sourceAmount, HashSet<Amount<?>> targetAmounts)
	{
		Amount<?> bestAmount = null;
		double smallestFactor = -1;
		for (Amount<?> targetAmount : targetAmounts)
		{
			Double convertFactor = convert(sourceAmount, targetAmount);
			if (smallestFactor == -1)
			{
				smallestFactor = convertFactor;
				bestAmount = targetAmount;
			}
			else if (smallestFactor > convertFactor)
			{
				smallestFactor = convertFactor;
				bestAmount = targetAmount;
			}
		}
		return bestAmount;
	}
}
