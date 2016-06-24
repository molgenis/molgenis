package org.molgenis.util;

public interface Cell<T>
{
	public Integer getId();

	public String getKey();

	public T getValue();
}
