package org.molgenis.data.mapper.categorymapper;

import javax.measure.unit.NonSI;

import org.jscience.physics.amount.Amount;

public class DailyAmountConvertor implements AmountConvertor
{
	private final static String CRITERIA = "daily";
	private final static Amount<?> AMOUNT = Amount.valueOf((double) 1, NonSI.DAY);

	public boolean matchCriteria(String description)
	{
		return description.toLowerCase().matches(CRITERIA);
	}

	@Override
	public Amount<?> getAmount(String description)
	{
		return AMOUNT;
	}
}
