package org.molgenis.data.mapper.algorithmgenerator.categorymapper.convertor;

import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;

import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.molgenis.data.mapper.algorithmgenerator.bean.AmountWrapper;
import org.molgenis.data.mapper.algorithmgenerator.categorymapper.FrequencyCategoryMapperUtil;

public class NumberAmountConvertor extends AmountConvertor
{
	public boolean matchCriteria(String description)
	{
		return true;
	}

	AmountWrapper getInternalAmount(String description)
	{
		List<Double> extractNumbers = FrequencyCategoryMapperUtil.extractNumbers(description);
		Unit<?> unit = FrequencyCategoryMapperUtil.findDurationUnit(description);
		Collections.sort(extractNumbers);

		if (extractNumbers.size() == 1)
		{
			return AmountWrapper.create(Amount.valueOf(extractNumbers.get(0), unit));
		}
		else if (extractNumbers.size() > 1)
		{
			return AmountWrapper.create(Amount.rangeOf(extractNumbers.get(0),
					extractNumbers.get(extractNumbers.size() - 1), unit));
		}

		return null;
	}

	Double average(Set<Double> values)
	{
		OptionalDouble average = values.stream().mapToDouble(Double::doubleValue).average();
		return average.isPresent() ? average.getAsDouble() : null;
	}
}
