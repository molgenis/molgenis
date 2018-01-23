package org.molgenis.semanticmapper.algorithmgenerator.categorymapper.convertor;

import org.jscience.physics.amount.Amount;
import org.molgenis.semanticmapper.algorithmgenerator.bean.AmountWrapper;
import org.molgenis.semanticmapper.algorithmgenerator.categorymapper.CategoryMapperUtil;

import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

public abstract class AmountConvertor
{
	public static final Unit<?> STANDARD_PER_WEEK_UNIT = NonSI.WEEK.inverse();

	public abstract boolean matchCriteria(String description);

	abstract AmountWrapper getInternalAmount(String description);

	public AmountWrapper getAmount(String description)
	{
		AmountWrapper amountWrapper = getInternalAmount(description);
		if (amountWrapper != null && CategoryMapperUtil.containNegativeAdjectives(description))
		{
			Amount<?> amount = amountWrapper.getAmount();
			Amount<?> newAmount = Amount.rangeOf((double) 0, amount.getEstimatedValue(), amount.getUnit());
			amountWrapper = AmountWrapper.create(newAmount, amountWrapper.isDetermined());
		}
		return amountWrapper;
	}
}