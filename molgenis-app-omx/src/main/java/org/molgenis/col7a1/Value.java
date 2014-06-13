package org.molgenis.col7a1;

public class Value
{
	private final String value;

	Value(String value)
	{
		this.value = value;
	}

	public String getValue()
	{
		return this.value;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}

		if (this == obj)
		{
			return true;
		}

		if (this.getClass() != obj.getClass())
		{
			return false;
		}

		final Value v = (Value) obj;
		return this.value.equals(v.value);
	}
}
