package org.molgenis.semanticmapper.algorithmgenerator.categorymapper;

import org.jscience.physics.amount.Amount;
import org.molgenis.semanticmapper.algorithmgenerator.bean.AmountWrapper;
import org.molgenis.semanticmapper.algorithmgenerator.bean.Category;
import org.molgenis.semanticmapper.algorithmgenerator.rules.CategoryMatchQuality;
import org.molgenis.semanticmapper.algorithmgenerator.rules.CategoryRule;

import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;
import java.util.List;

public class FrequencyCategoryMapper extends CategoryMapper
{
	private static final Unit<?> STANDARD_UNIT = NonSI.WEEK.inverse();

	private static final double STANDARD_ERROR = 1;

	public FrequencyCategoryMapper(List<CategoryRule> rules)
	{
		super(rules);
	}

	public Category findBestCategoryMatch(Category sourceCategory, List<Category> targetCategories)
	{
		Category bestTargetCategory = null;
		double smallestFactor = -1;
		for (Category targetCategory : targetCategories)
		{
			Double convertFactor = convert(sourceCategory.getAmountWrapper(), targetCategory.getAmountWrapper());

			if (convertFactor == null) continue;

			if (smallestFactor == -1)
			{
				smallestFactor = convertFactor;
				bestTargetCategory = targetCategory;
			}
			else if (smallestFactor > convertFactor)
			{
				smallestFactor = convertFactor;
				bestTargetCategory = targetCategory;
			}
		}
		return bestTargetCategory;
	}

	public CategoryMatchQuality<?> applyCustomRules(Category sourceCategory, Category targetCategory)
	{
		throw new UnsupportedOperationException();
	}

	Double convert(AmountWrapper sourceAmountWrapper, AmountWrapper targetAmountWrapper)
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

	Amount<?> determineAmount(Amount<?> amountToDetermine, Amount<?> determinedAmount)
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

	Double convertFactor(Amount<?> convertedSourceAmount, Amount<?> targetAmount)
	{
		double lowerBoundDiff = Math.abs(targetAmount.getMinimumValue() - convertedSourceAmount.getMinimumValue());
		double upperBoundDiff = Math.abs(targetAmount.getMaximumValue() - convertedSourceAmount.getMaximumValue());

		return (upperBoundDiff + lowerBoundDiff) / 2;
	}

	boolean isMaxValueUndetermined(Amount<?> amount1)
	{
		return CategoryMapperUtil.isAmountRanged(amount1) && amount1.getMaximumValue() == Double.MAX_VALUE;
	}
}
