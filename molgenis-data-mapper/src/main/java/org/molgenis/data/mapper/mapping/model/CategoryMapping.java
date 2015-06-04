package org.molgenis.data.mapper.mapping.model;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;

@AutoValue
public abstract class CategoryMapping<S, T>
{
	private Gson gson = new Gson();

	public abstract String getSourceAttributeName();

	public abstract Map<S, T> getMap();

	@Nullable
	public abstract T getDefaultValue();

	@Nullable
	public abstract T getNullValue();

	public static <S, T> CategoryMapping<S, T> create(String sourceAttributeName, Map<S, T> map)
	{
		return new AutoValue_CategoryMapping<S, T>(sourceAttributeName, map, null, null);
	}

	public static <S, T> CategoryMapping<S, T> create(String sourceAttributeName, Map<S, T> map, T defaultValue)
	{
		return new AutoValue_CategoryMapping<S, T>(sourceAttributeName, map, defaultValue, null);
	}

	public static <S, T> CategoryMapping<S, T> create(String sourceAttributeName, Map<S, T> map, T defaultValue,
			T nullValue)
	{
		return new AutoValue_CategoryMapping<S, T>(sourceAttributeName, map, defaultValue, nullValue);
	}

	public String getAlgorithm()
	{
		if (getNullValue() != null)
		{
			return String.format("$('%s').map(%s, %s, %s).value();", getSourceAttributeName(), gson.toJson(getMap()),
					gson.toJson(getDefaultValue()), gson.toJson(getNullValue()));
		}
		if (getDefaultValue() != null)
		{
			return String.format("$('%s').map(%s, %s).value();", getSourceAttributeName(), gson.toJson(getMap()),
					gson.toJson(getDefaultValue()));
		}
		return String.format("$('%s').map(%s).value();", getSourceAttributeName(), gson.toJson(getMap()));
	}

	public static Object create(String algorithm)
	{
		Pattern p = Pattern.compile("$\\('(.+)'\\).map\\((.+)(, (.+)(, (.+))?)?\\).value\\(\\);?");
		Matcher m = p.matcher(algorithm);
		if (algorithm.match())
		{

		}
	}
}
