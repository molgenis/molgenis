package org.molgenis.data.support;

import java.sql.Timestamp;
import java.util.List;

import org.molgenis.data.DataConverter;
import org.molgenis.data.Entity;
import org.springframework.beans.BeanUtils;

public abstract class AbstractEntity implements Entity
{
	private static final long serialVersionUID = 1L;

	@Override
	public void set(Entity entity, boolean strict)
	{
		this.set(entity);

	}

	@Override
	public String getString(String attributeName)
	{
		return DataConverter.toString(get(attributeName));
	}

	@Override
	public Integer getInt(String attributeName)
	{
		return DataConverter.toInt(get(attributeName));
	}

	@Override
	public Long getLong(String attributeName)
	{
		return DataConverter.toLong(get(attributeName));
	}

	@Override
	public Boolean getBoolean(String attributeName)
	{
		return DataConverter.toBoolean(get(attributeName));
	}

	@Override
	public Double getDouble(String attributeName)
	{
		return DataConverter.toDouble(get(attributeName));
	}

	@Override
	public java.sql.Date getDate(String attributeName)
	{
		return DataConverter.toDate(get(attributeName));
	}

	@Override
	public java.util.Date getUtilDate(String attributeName)
	{
		return DataConverter.toUtilDate(get(attributeName));
	}

	@Override
	public Timestamp getTimestamp(String attributeName)
	{
		return DataConverter.toTimestamp(get(attributeName));
	}

	@Override
	public List<String> getList(String attributeName)
	{
		return DataConverter.toList(get(attributeName));
	}

	@Override
	public List<Integer> getIntList(String attributeName)
	{
		return DataConverter.toIntList(get(attributeName));
	}

	public static boolean isObjectRepresentation(String objStr)
	{
		int left = objStr.indexOf('(');
		int right = objStr.lastIndexOf(')');
		return (left == -1 || right == -1) ? false : true;
	}

	public static <T extends Entity> T setValuesFromString(String objStr, Class<T> klass)
	{
		T result = BeanUtils.instantiateClass(klass);

		int left = objStr.indexOf('(');
		int right = objStr.lastIndexOf(')');

		String content = objStr.substring(left + 1, right);

		String[] attrValues = content.split(" ");
		for (String attrValue : attrValues)
		{
			String[] av = attrValue.split("=");
			String attr = av[0];
			String value = av[1];
			if (value.charAt(0) == '\'' && value.charAt(value.length() - 1) == '\'')
			{
				value = value.substring(1, value.length() - 1);
			}
			result.set(attr, value);
		}
		return result;
	}
}
