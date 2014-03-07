package org.molgenis.omx.utils;

import org.molgenis.util.Cell;

public class ValueCell<T> implements Cell<T>
{
	private final Integer id;
	private final String key;
	private final T value;

	public ValueCell(T value)
	{
		this(null, value);
	}

	public ValueCell(String key, T value)
	{
		this(null, key, value);
	}

	public ValueCell(Integer id, String key, T value)
	{
		if (value == null) throw new IllegalArgumentException("value is null");
		this.id = id;
		this.key = key;
		this.value = value;
	}

	@Override
	public Integer getId()
	{
		return id;
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
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		ValueCell other = (ValueCell) obj;
		if (id == null)
		{
			if (other.id != null) return false;
		}
		else if (!id.equals(other.id)) return false;
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
