package org.molgenis.diseasematcher.controller;

import java.util.List;

public class FindDiseasesRequest
{
	private List<String> geneSymbols;

	public List<String> getGeneSymbols()
	{
		return geneSymbols;
	}

	public void setGeneSymbols(List<String> geneSymbols)
	{
		this.geneSymbols = geneSymbols;
	}

}
