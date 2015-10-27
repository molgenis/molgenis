package org.molgenis.data.mapper.algorithmgenerator.bean;

import javax.annotation.Nullable;

import org.molgenis.gson.AutoGson;

import com.google.auto.value.AutoValue;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Category.class)
public abstract class Category
{
	public abstract String getCode();

	public abstract String getLabel();

	@Nullable
	public abstract AmountWrapper getAmountWrapper();

	public static Category create(String code, String label)
	{
		return new AutoValue_Category(code, label, null);
	}

	public static Category create(String code, String label, AmountWrapper amountWrapper)
	{
		return new AutoValue_Category(code, label, amountWrapper);
	}
}
