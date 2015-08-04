package org.molgenis.data.mapper.algorithmgenerator.bean;

import org.molgenis.gson.AutoGson;

import com.google.auto.value.AutoValue;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Category.class)
public abstract class Category
{
	public abstract int getCode();

	public abstract String getLabel();

	public static Category create(int code, String label)
	{
		return new AutoValue_Category(code, label);
	}
}
