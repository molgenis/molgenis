package org.molgenis.data.meta;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static java.util.stream.Collectors.toList;

public enum AttributeType
{
	BOOL, CATEGORICAL, CATEGORICAL_MREF, COMPOUND, DATE, DATE_TIME, DECIMAL, EMAIL(255L), ENUM(255L), FILE, HTML(
		65535L), HYPERLINK(255L), INT, LONG, MREF, ONE_TO_MANY, SCRIPT(65535L), STRING(255L), TEXT(65535L), XREF;

	private static final Map<String, AttributeType> strValMap;

	static
	{
		AttributeType[] dataTypes = AttributeType.values();
		strValMap = newHashMapWithExpectedSize(dataTypes.length);
		for (AttributeType dataType : dataTypes)
		{
			strValMap.put(getValueString(dataType), dataType);
		}
	}

	private final Long maxLength;

	AttributeType()
	{
		this.maxLength = null;
	}

	AttributeType(long maxLength)
	{
		this.maxLength = maxLength;
	}

	public Long getMaxLength()
	{
		return maxLength;
	}

	/**
	 * Returns the enum value for the given value string
	 *
	 * @param valueString value string
	 * @return enum value
	 */
	public static AttributeType toEnum(String valueString)
	{
		return strValMap.get(normalize(valueString));
	}

	/**
	 * Returns the value string for the given enum value
	 *
	 * @param value enum value
	 * @return value string
	 */
	public static String getValueString(AttributeType value)
	{
		return normalize(value.toString());
	}

	/**
	 * Returns the value strings for all enum types in the defined enum order
	 *
	 * @return value strings
	 */
	public static List<String> getOptionsLowercase()
	{
		return Arrays.stream(values()).map(AttributeType::getValueString).collect(toList());
	}

	private static String normalize(String valueString)
	{
		return StringUtils.remove(valueString, '_').toLowerCase();
	}
}
