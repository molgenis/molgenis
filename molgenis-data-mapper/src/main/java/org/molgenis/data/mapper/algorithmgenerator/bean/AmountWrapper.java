package org.molgenis.data.mapper.algorithmgenerator.bean;

import javax.annotation.Nullable;

import org.jscience.physics.amount.Amount;
import org.molgenis.gson.AutoGson;

import com.google.auto.value.AutoValue;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AmountWrapper.class)
public abstract class AmountWrapper
{
	@Nullable
	public abstract Amount<?> getAmount();

	@Nullable
	public abstract boolean isDetermined();

	public static AmountWrapper create(Amount<?> amount)
	{
		return new AutoValue_AmountWrapper(amount, true);
	}

	public static AmountWrapper create(Amount<?> amount, boolean determined)
	{
		return new AutoValue_AmountWrapper(amount, determined);
	}
}
