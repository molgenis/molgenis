package org.molgenis.data.mapper.algorithmgenerator.categorymapper.convertor;

import javax.measure.unit.NonSI;

import org.jscience.physics.amount.Amount;
import org.molgenis.data.mapper.algorithmgenerator.bean.AmountWrapper;

public class DailyAmountConvertor extends AmountConvertor
{
	private final static String CRITERIA = "daily";
	private final static Amount<?> AMOUNT = Amount.valueOf((double) 1, NonSI.DAY.inverse()).to(STANDARD_PER_WEEK_UNIT);

	public boolean matchCriteria(String description)
	{
		return description.toLowerCase().contains(CRITERIA);
	}

	AmountWrapper getInternalAmount(String description)
	{
		return AmountWrapper.create(AMOUNT);
	}
}
