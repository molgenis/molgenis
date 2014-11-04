package org.molgenis.mutationdb;

public class Value
{
	private final String value;

	public Value(String value)
	{
		this.value = value;
	}

	public String getValue()
	{
		return this.value;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Value other = (Value) obj;
		if (value == null)
		{
			if (other.value != null) return false;
		}
		else if (!value.equals(other.value)) return false;
		return true;
	}
}
