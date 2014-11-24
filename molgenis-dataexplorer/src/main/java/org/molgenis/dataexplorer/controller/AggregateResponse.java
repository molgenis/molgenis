package org.molgenis.dataexplorer.controller;

import java.util.List;
import java.util.Set;

public class AggregateResponse
{
	private final List<List<Long>> matrix;
	private final Set<String> xLabels;
	private final Set<String> yLabels;

	public AggregateResponse(List<List<Long>> matrix, Set<String> xLabels, Set<String> yLabels)
	{
		this.matrix = matrix;
		this.xLabels = xLabels;
		this.yLabels = yLabels;
	}

	public List<List<Long>> getMatrix()
	{
		return matrix;
	}

	public Set<String> getxLabels()
	{
		return xLabels;
	}

	public Set<String> getyLabels()
	{
		return yLabels;
	}

}
