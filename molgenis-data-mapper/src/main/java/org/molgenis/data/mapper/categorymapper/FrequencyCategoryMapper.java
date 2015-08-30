package org.molgenis.data.mapper.categorymapper;

import java.util.HashSet;
import java.util.List;

import javax.measure.unit.NonSI;
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

	private static final Unit<?> STANDARD_UNIT = NonSI.WEEK.inverse();

	public Double convert(Amount<?> sourceAmount, Amount<?> targetAmount)
	{
		if (sourceAmount != null && targetAmount != null)
		{
			Unit<?> standardUnit = targetAmount.getUnit();
			if (sourceAmount.getUnit().isCompatible(standardUnit))
			{
				return convertFactor(sourceAmount.to(STANDARD_UNIT), targetAmount.to(STANDARD_UNIT));
			}
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

	Double convertFactor(Amount<?> convertedSourceAmount, Amount<?> targetAmount)
	{
		int targetMax = (int) targetAmount.getMaximumValue();
		int targetMini = (int) targetAmount.getMinimumValue();
		int sourceMini = (int) convertedSourceAmount.getMinimumValue();
		int sourceMax = (int) convertedSourceAmount.getMaximumValue();

		if (targetMax < sourceMini || sourceMax < targetMini)
		{
			return null;
		}

		double lowerBoundDiff = targetAmount.getMinimumValue() - convertedSourceAmount.getMinimumValue();
		double upperBoundDiff = targetAmount.getMaximumValue() - convertedSourceAmount.getMaximumValue();

		return Math.abs(upperBoundDiff + lowerBoundDiff) / 2;
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
