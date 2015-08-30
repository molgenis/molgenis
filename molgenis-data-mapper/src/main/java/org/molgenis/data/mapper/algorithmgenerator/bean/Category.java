package org.molgenis.data.mapper.algorithmgenerator.bean;

import javax.annotation.Nullable;

import org.jscience.physics.amount.Amount;
import org.molgenis.gson.AutoGson;

import com.google.auto.value.AutoValue;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Category.class)
public abstract class Category
{
	public abstract int getCode();

	public abstract String getLabel();

	@Nullable
	public abstract Amount<?> getAmount();

	public static Category create(int code, String label)
	{
		return new AutoValue_Category(code, label, null);
	}

	public static Category create(int code, String label, Amount<?> amount)
	{
		return new AutoValue_Category(code, label, amount);
	}
}
