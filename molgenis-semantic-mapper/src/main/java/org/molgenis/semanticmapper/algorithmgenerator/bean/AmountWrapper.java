package org.molgenis.semanticmapper.algorithmgenerator.bean;

import com.google.auto.value.AutoValue;
import org.jscience.physics.amount.Amount;
import org.molgenis.core.gson.AutoGson;

import javax.annotation.Nullable;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AmountWrapper.class)
public abstract class AmountWrapper
{
	@Nullable
	public abstract Amount<?> getAmount();

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
