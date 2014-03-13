package org.molgenis.dataexplorer.controller;

import java.util.Map;

public class AggregateResponse
{
	private final Map<String, Long> hashCategories;

	public AggregateResponse(Map<String, Long> hashCategories)
	{
		this.hashCategories = hashCategories;
	}

	public Map<String, Long> getHashCategories()
	{
		return hashCategories;
	}
}
