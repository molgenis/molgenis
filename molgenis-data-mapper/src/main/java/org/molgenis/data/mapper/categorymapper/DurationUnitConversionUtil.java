package org.molgenis.data.mapper.categorymapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.measure.converter.UnitConverter;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import com.google.common.collect.Sets;

public class DurationUnitConversionUtil
{
	private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+\\.?\\d*");
	private static final String NON_LETTER_REGEX = "[^a-zA-Z0-9]";
	private static final List<Unit<?>> DURATION_UNITS;
	static
	{
		DURATION_UNITS = Arrays.asList(SI.SECOND.inverse(), NonSI.MINUTE.inverse(), NonSI.HOUR.inverse(),
				NonSI.DAY.inverse(), NonSI.WEEK.inverse(), NonSI.MONTH.inverse(), NonSI.YEAR.inverse());

		Collections.sort(DURATION_UNITS, new Comparator<Unit<?>>()
		{
			public int compare(Unit<?> o1, Unit<?> o2)
			{
				UnitConverter converterTo = o1.getConverterTo(o2);
				if (converterTo.convert(1) > 1)
				{
					return 0;
				}
				else
				{
					return 1;
				}
			}
		});
	}

	private static final Set<String> POSITIVE_ADJECTIVES;
	static
	{
		POSITIVE_ADJECTIVES = new HashSet<String>();
		POSITIVE_ADJECTIVES.add("almost");
	}

	private static final Set<String> NEGATIVE_ADJECTIVES;
	static
	{
		NEGATIVE_ADJECTIVES = new HashSet<String>();
		NEGATIVE_ADJECTIVES.add("never");
		NEGATIVE_ADJECTIVES.add("less");
		NEGATIVE_ADJECTIVES.add("fewer");
	}

	private static final Map<String, Integer> WORD_TO_NUMBER_MAP;
	private static final double STANDARD_ERROR = 0.0000000001;
	static
	{
		WORD_TO_NUMBER_MAP = new HashMap<String, Integer>();
		WORD_TO_NUMBER_MAP.put("one", 1);
		WORD_TO_NUMBER_MAP.put("two", 2);
		WORD_TO_NUMBER_MAP.put("three", 3);
		WORD_TO_NUMBER_MAP.put("four", 4);
		WORD_TO_NUMBER_MAP.put("five", 5);
		WORD_TO_NUMBER_MAP.put("six", 6);
		WORD_TO_NUMBER_MAP.put("seven", 7);
		WORD_TO_NUMBER_MAP.put("eight", 8);
		WORD_TO_NUMBER_MAP.put("nine", 9);
		WORD_TO_NUMBER_MAP.put("ten", 10);

		WORD_TO_NUMBER_MAP.put("once", 1);
		WORD_TO_NUMBER_MAP.put("twice", 2);
	}

	public static boolean containNegativeAdjectives(String description)
	{
		String lowerCase = description.toLowerCase();
		return NEGATIVE_ADJECTIVES.stream().anyMatch(adj -> lowerCase.contains(adj));
	}

	public static Unit<?> getMoreSpecificUnit(Unit<?> unit)
	{
		int indexOf = DURATION_UNITS.indexOf(unit);
		if (indexOf <= 0)
		{
			return unit;
		}
		else
		{
			return DURATION_UNITS.get(--indexOf);
		}
	}

	public static String convertWordToNumber(String description)
	{
		StringBuilder stringBuilder = new StringBuilder();
		for (String token : description.toLowerCase().split(NON_LETTER_REGEX))
		{
			if (stringBuilder.length() > 0) stringBuilder.append(' ');
			if (WORD_TO_NUMBER_MAP.containsKey(token))
			{
				stringBuilder.append(WORD_TO_NUMBER_MAP.get(token));
			}
			else
			{
				stringBuilder.append(token);
			}
		}
		return stringBuilder.toString();
	}

	public static Unit<?> findDurationUnit(String description)
	{
		Set<String> tokens = Sets.newHashSet(description.toLowerCase().split(NON_LETTER_REGEX));

		for (Unit<?> unit : DURATION_UNITS)
		{
			if (tokens.contains(unit.inverse().toString().toLowerCase())) return unit;
		}
		return null;
	}

	public static Set<Double> extractNumbers(String description)
	{
		Set<Double> extractedNumbers = new HashSet<Double>();
		String lowerCasedDesc = description.toLowerCase();
		Matcher mather = NUMBER_PATTERN.matcher(lowerCasedDesc);
		while (mather.find())
		{
			extractedNumbers.add(Double.parseDouble(mather.group()));
		}
		return extractedNumbers;
	}

	public static boolean containsNegativeAdjectives(String description)
	{
		String lowerCasedDesc = description.toLowerCase();
		return NEGATIVE_ADJECTIVES.stream().anyMatch(adj -> lowerCasedDesc.contains(adj));
	}

	public static boolean isAmountRanged(Amount<?> amount)
	{
		return amount.getMaximumValue() - amount.getMinimumValue() > STANDARD_ERROR;
	}
}
