package org.molgenis.data.mapper.categorymapper;

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

	public Amount<?> convertCategory(Amount<?> amount1, Unit<?> unit)
	{
		if (amount1.getUnit().isCompatible(unit))
		{
			if (isMaxValueUndetermined(amount1))
			{
				double maxValue = unit.getConverterTo(amount1.getUnit()).convert((double) 1);
				amount1 = Amount.rangeOf(amount1.getMinimumValue(), maxValue, amount1.getUnit());
			}
			return amount1.to(unit);
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

	boolean isMaxValueUndetermined(Amount<?> amount1)
	{
		return DurationUnitConversionUtil.isAmountRanged(amount1) && amount1.getMaximumValue() == Double.MAX_VALUE;
	}
}
