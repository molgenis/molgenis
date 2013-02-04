package org.molgenis.ui;

/** Supported icons */
public enum Icon
{
	SEARCH;

	@Override
	public String toString()
	{
		return this.name().toLowerCase();
	}

}
