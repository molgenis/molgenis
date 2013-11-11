package org.molgenis.dataexplorer.controller;

import java.util.Map;

public class AggregateResponse {
	
	private final Map<String,Integer> hashCategories;
	
	public AggregateResponse(Map<String,Integer> hashCategories)
	{
		this.hashCategories = hashCategories;
	}

	public Map<String, Integer> getHashCategories() {
		return hashCategories;
	}
}
