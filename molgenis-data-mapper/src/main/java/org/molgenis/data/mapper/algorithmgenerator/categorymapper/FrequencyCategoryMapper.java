package org.molgenis.data.mapper.algorithmgenerator.categorymapper;

import java.util.List;

import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.molgenis.data.mapper.algorithmgenerator.bean.AmountWrapper;
import org.molgenis.data.mapper.algorithmgenerator.categorymapper.convertor.AmountConvertor;
import org.molgenis.data.mapper.algorithmgenerator.categorymapper.convertor.DailyAmountConvertor;
import org.molgenis.data.mapper.algorithmgenerator.categorymapper.convertor.NumberAmountConvertor;
import org.molgenis.data.mapper.algorithmgenerator.categorymapper.convertor.SeveralTimesConvertor;

import com.google.common.collect.Lists;

public class FrequencyCategoryMapper
{
	private static final List<AmountConvertor> CONVERTORS = Lists.newArrayList(new DailyAmountConvertor(),
			new SeveralTimesConvertor(), new NumberAmountConvertor());

	private static final Unit<?> STANDARD_UNIT = NonSI.WEEK.inverse();

	private static final double STANDARD_ERROR = 1;

	public Double convert(AmountWrapper sourceAmountWrapper, AmountWrapper targetAmountWrapper)
	{
		if (sourceAmountWrapper != null && targetAmountWrapper != null)
		{
			Amount<?> sourceAmount = sourceAmountWrapper.getAmount().to(STANDARD_UNIT);
			Amount<?> targetAmount = targetAmountWrapper.getAmount().to(STANDARD_UNIT);

			if (unitsCompatible(sourceAmount, targetAmount))
			{
				if (!sourceAmountWrapper.isDetermined() && !targetAmountWrapper.isDetermined())
				{
					if (sourceAmountWrapper.getAmount().getUnit().equals(targetAmountWrapper.getAmount().getUnit()))
					{
						return (double) 0;
					}
				}

				if (!sourceAmountWrapper.isDetermined())
				{
					sourceAmount = determineAmount(sourceAmount, targetAmount);
				}

				if (!targetAmountWrapper.isDetermined())
				{
					targetAmount = determineAmount(targetAmount, sourceAmount);
				}

				return convertFactor(sourceAmount, targetAmount);
			}
		}

		return null;
	}

	private Amount<?> determineAmount(Amount<?> amountToDetermine, Amount<?> determinedAmount)
	{
		Amount<?> convertedAmount = amountToDetermine.to(determinedAmount.getUnit());

		double maxValue = convertedAmount.getMaximumValue();
		double minValue = convertedAmount.getMinimumValue();

		double determinedMaxValue = determinedAmount.getMaximumValue();
		double determinedMinValue = determinedAmount.getMinimumValue();

		if (determinedMaxValue > minValue && maxValue > determinedMaxValue)
		{
			maxValue = determinedMaxValue + STANDARD_ERROR;
		}

		if (determinedMinValue > minValue && determinedMinValue < maxValue)
		{
			minValue = determinedMinValue - STANDARD_ERROR;
		}

		return Amount.rangeOf(minValue, maxValue, convertedAmount.getUnit());
	}

	boolean unitsCompatible(Amount<?> sourceAmount, Amount<?> targetAmount)
	{
		return sourceAmount.getUnit().isCompatible(STANDARD_UNIT) && targetAmount.getUnit().isCompatible(STANDARD_UNIT)
				&& sourceAmount.getUnit().isCompatible(targetAmount.getUnit());
	}

	public AmountWrapper convertDescriptionToAmount(String description)
	{
		String cleanedDescription = FrequencyCategoryMapperUtil.convertWordToNumber(description);
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
		double lowerBoundDiff = Math.abs(targetAmount.getMinimumValue() - convertedSourceAmount.getMinimumValue());
		double upperBoundDiff = Math.abs(targetAmount.getMaximumValue() - convertedSourceAmount.getMaximumValue());

		return (upperBoundDiff + lowerBoundDiff) / 2;
	}

	boolean isMaxValueUndetermined(Amount<?> amount1)
	{
		return FrequencyCategoryMapperUtil.isAmountRanged(amount1) && amount1.getMaximumValue() == Double.MAX_VALUE;
	}
}
