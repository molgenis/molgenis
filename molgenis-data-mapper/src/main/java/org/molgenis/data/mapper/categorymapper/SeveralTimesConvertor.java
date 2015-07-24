package org.molgenis.data.mapper.categorymapper;

import java.util.Set;

import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import com.google.common.collect.Sets;

public class SeveralTimesConvertor implements AmountConvertor
{
	private final static Set<String> CRITERIA = Sets.newHashSet("several times", "several");

	public boolean matchCriteria(String description)
	{
		String lowerCase = description.toLowerCase();
		return CRITERIA.stream().anyMatch(keyWord -> lowerCase.contains(keyWord));
	}

	public Amount<?> getAmount(String description)
	{
		Unit<?> unit = DurationUnitConversionUtil.findDurationUnit(description);
		if (unit != null)
		{
			unit.getConverterTo(NonSI.DAY);
		}
		return null;
	}
}
