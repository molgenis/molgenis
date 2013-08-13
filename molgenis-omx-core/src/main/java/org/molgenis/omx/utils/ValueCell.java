package org.molgenis.omx.utils;

import org.molgenis.util.tuple.Cell;

public class ValueCell<T> implements Cell<T>
{
	private final String key;
	private final T value;

	public ValueCell(T value)
	{
		this(null, value);
	}

	public ValueCell(String key, T value)
	{
		if (value == null) throw new IllegalArgumentException("value is null");
		this.key = key;
		this.value = value;
	}

	@Override
	public String getKey()
	{
		return key;
	}

	@Override
	public T getValue()
	{
		return value;
	}

	@Override
	public String toString()
	{
		return value.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ValueCell<?> other = (ValueCell<?>) obj;
		if (key == null)
		{
			if (other.key != null) return false;
		}
		else if (!key.equals(other.key)) return false;
		if (value == null)
		{
			if (other.value != null) return false;
		}
		else if (!value.equals(other.value)) return false;
		return true;
	}
}
