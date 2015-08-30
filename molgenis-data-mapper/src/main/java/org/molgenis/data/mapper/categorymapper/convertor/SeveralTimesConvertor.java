package org.molgenis.data.mapper.categorymapper.convertor;

import java.util.Set;

import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.molgenis.data.mapper.categorymapper.DurationUnitConversionUtil;

import com.google.common.collect.Sets;

public class SeveralTimesConvertor extends AmountConvertor
{
	private final static Set<String> CRITERIA = Sets.newHashSet("several times", "several");

	public boolean matchCriteria(String description)
	{
		String lowerCase = description.toLowerCase();
		return CRITERIA.stream().anyMatch(keyWord -> lowerCase.contains(keyWord));
	}

	Amount<?> getInternalAmount(String description)
	{
		Unit<?> unit = DurationUnitConversionUtil.findDurationUnit(description);
		if (unit != null)
		{
			return Amount.rangeOf((double) 1, NonSI.DAY.inverse().getConverterTo(unit).convert(1), unit);
		}
		return null;
	}
}