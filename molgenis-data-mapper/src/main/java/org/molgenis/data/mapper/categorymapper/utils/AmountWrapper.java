package org.molgenis.data.mapper.categorymapper.utils;

import org.jscience.physics.amount.Amount;

public class AmountWrapper
{
	private Amount<?> amount;
	private boolean isDetermined;

	public AmountWrapper(Amount<?> amount, boolean isDetermined)
	{
		this.amount = amount;
		this.isDetermined = isDetermined;
	}

	public Amount<?> getAmount()
	{
		return amount;
	}

	public void setAmount(Amount<?> amount)
	{
		this.amount = amount;
	}

	public boolean isDetermined()
	{
		return isDetermined;
	}

	public void setDetermined(boolean isDetermined)
	{
		this.isDetermined = isDetermined;
	}
}
