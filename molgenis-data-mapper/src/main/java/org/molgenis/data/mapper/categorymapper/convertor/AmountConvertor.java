package org.molgenis.data.mapper.categorymapper.convertor;

import org.jscience.physics.amount.Amount;
import org.molgenis.data.mapper.categorymapper.DurationUnitConversionUtil;

public abstract class AmountConvertor
{
	public abstract boolean matchCriteria(String description);

	abstract Amount<?> getInternalAmount(String description);

	public Amount<?> getAmount(String description)
	{
		Amount<?> amount = getInternalAmount(description);
		if (DurationUnitConversionUtil.containNegativeAdjectives(description))
		{
			double estimatedValue = amount.getEstimatedValue();
			return Amount.rangeOf((double) 0, estimatedValue, amount.getUnit());
		}
		return amount;
	}
}