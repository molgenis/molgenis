package org.molgenis.data.mapper.algorithmgenerator.categorymapper.convertor;

import java.util.Set;

import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.molgenis.data.mapper.algorithmgenerator.bean.AmountWrapper;
import org.molgenis.data.mapper.algorithmgenerator.categorymapper.CategoryMapperUtil;

import com.google.common.collect.Sets;

public class SeveralTimesConvertor extends AmountConvertor
{
	private final static Set<String> CRITERIA = Sets.newHashSet("several times", "several");

	public boolean matchCriteria(String description)
	{
		String lowerCase = description.toLowerCase();
		return CRITERIA.stream().anyMatch(keyWord -> lowerCase.contains(keyWord));
	}

	AmountWrapper getInternalAmount(String description)
	{
		Unit<?> unit = CategoryMapperUtil.findDurationUnit(description);
		if (unit != null && unit.isCompatible(STANDARD_UNIT))
		{
			return AmountWrapper.create(
					Amount.rangeOf((double) 2, NonSI.DAY.inverse().getConverterTo(unit).convert(1) - 1, unit).to(
							STANDARD_UNIT), false);
		}
		return null;
	}
}