package org.molgenis.util;

public interface Cell<T>
{
	Integer getId();

	String getKey();

	T getValue();
}
