package org.molgenis.data.mapper.mapping.model;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@AutoValue
public abstract class CategoryMapping<S, T>
{
	private static Gson gson = new Gson();

	public abstract String getSourceAttributeName();

	public abstract Map<S, T> getMap();

	@Nullable
	public abstract T getDefaultValue();

	@Nullable
	public abstract T getNullValue();

	public abstract boolean isNullValueUndefined();

	public static <S, T> CategoryMapping<S, T> create(String sourceAttributeName, Map<S, T> map)
	{
		return new AutoValue_CategoryMapping<S, T>(sourceAttributeName, map, null, null, true);
	}

	public static <S, T> CategoryMapping<S, T> create(String sourceAttributeName, Map<S, T> map, T defaultValue)
	{
		return new AutoValue_CategoryMapping<S, T>(sourceAttributeName, map, defaultValue, null, true);
	}

	public static <S, T> CategoryMapping<S, T> create(String sourceAttributeName, Map<S, T> map, T defaultValue,
			T nullValue)
	{
		return new AutoValue_CategoryMapping<S, T>(sourceAttributeName, map, defaultValue, nullValue, false);
	}

	public String getAlgorithm()
	{
		if (!isNullValueUndefined())
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

	public static <S, T> CategoryMapping<S, T> createEmpty(String attributeName)
	{
		return create(attributeName, Collections.emptyMap());
	}

	public static <S, T> CategoryMapping<S, T> create(String algorithm)
	{
		Pattern p = Pattern
				.compile("\\$\\('(?<attr>.+)'\\)\\.map\\((?<map>\\{.+\\})(,?(?<default>[^,]+)(,(?<null>.+))?)?\\).value\\(\\);?");
		Matcher m = p.matcher(algorithm.replace("\\s", ""));
		if (!m.matches())
		{
			return null;
		}
		// cannot instantiate generic types due to type erasure, so must create them using a TypeToken
		Type mapType = new TypeToken<Map<S, T>>()
		{
		}.getType();
		Type tType = new TypeToken<T>()
		{
		}.getType();

		String attr = m.group("attr");
		Map<S, T> map = gson.fromJson(m.group("map"), mapType);
		T defaultValue = gson.fromJson(m.group("default"), tType);
		String nullValue = m.group("null");
		if (nullValue == null)
		{
			return CategoryMapping.<S, T> create(attr, map, defaultValue);
		}
		else
		{
			T nullValueJson = gson.fromJson(nullValue, tType);
			return CategoryMapping.<S, T> create(attr, map, defaultValue, nullValueJson);
		}
	}
}
