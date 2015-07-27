package org.molgenis.data.mapper.categorymapper.convertor;

import javax.measure.unit.NonSI;

import org.jscience.physics.amount.Amount;

public class DailyAmountConvertor extends AmountConvertor
{
	private final static String CRITERIA = "daily";
	private final static Amount<?> AMOUNT = Amount.valueOf((double) 1, NonSI.DAY.inverse());

	public boolean matchCriteria(String description)
	{
		return description.toLowerCase().contains(CRITERIA);
	}

	Amount<?> getInternalAmount(String description)
	{
		return AMOUNT;
	}
}
