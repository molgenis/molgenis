package org.molgenis.semanticmapper.algorithmgenerator.categorymapper.convertor;

import com.google.common.collect.Sets;
import org.jscience.physics.amount.Amount;
import org.molgenis.semanticmapper.algorithmgenerator.bean.AmountWrapper;
import org.molgenis.semanticmapper.algorithmgenerator.categorymapper.CategoryMapperUtil;

import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;
import java.util.Set;

/**
 * The several times converter is able to convert any frequency related categories, in which 'several times' have
 * occurred, to a quantifiable amount. It indicates that the frequency, with which a person does certain activities or
 * consumes food, is more than 2 times, therefore the lower bound is set to 3 times (inclusive), which is the definition
 * used by HOP project. The upper bound is calculated using the formula (1 time / per day).convertTo(currentUnit) - 1,
 * which is decided upon the unit detected from the description. E.g. several times per week will be converted to 3-6
 * times a week. Several times per month will be converted to 3-30 times per month.
 *
 * @author chaopang
 */
public class SeveralTimesConvertor extends AmountConvertor
{
	private final static Set<String> CRITERIA = Sets.newHashSet("several times", "several");

	public boolean matchCriteria(String description)
	{
		String lowerCase = description.toLowerCase();
		return CRITERIA.stream().anyMatch(lowerCase::contains);
	}

	AmountWrapper getInternalAmount(String description)
	{
		Unit<?> unit = CategoryMapperUtil.findDurationUnit(description);
		if (unit != null && unit.isCompatible(STANDARD_PER_WEEK_UNIT))
		{
			return AmountWrapper.create(
					Amount.rangeOf((double) 3, NonSI.DAY.inverse().getConverterTo(unit).convert(1) - 1, unit)
						  .to(STANDARD_PER_WEEK_UNIT), false);
		}
		return null;
	}
}