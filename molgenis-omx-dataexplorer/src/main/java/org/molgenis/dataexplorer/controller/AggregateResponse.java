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
	/*
	 * private final List<List<Long>> matrix; private final Set<String> xLabels; private final Set<String> yLabels;
	 * 
	 * public AggregateResponse(List<List<Long>> matrix, Set<String> xLabels, Set<String> yLabels) { this.matrix =
	 * matrix; this.xLabels = xLabels; this.yLabels = yLabels; }
	 * 
	 * public List<List<Long>> getMatrix() { return matrix; }
	 * 
	 * public Set<String> getxLabels() { return xLabels; }
	 * 
	 * public Set<String> getyLabels() { return yLabels; }
	 */
}
