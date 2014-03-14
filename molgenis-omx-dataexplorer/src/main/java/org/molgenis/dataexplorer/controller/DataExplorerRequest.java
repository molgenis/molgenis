package org.molgenis.dataexplorer.controller;

public class DataExplorerRequest
{
	public enum TAB
	{
		DATA, AGGREGATE, CHART
	};

	private String dataset;
	private boolean wizard = false;
	private TAB tab = TAB.DATA;

	public String getDataset()
	{
		return dataset;
	}

	public void setDataset(String dataset)
	{
		this.dataset = dataset;
	}

	public boolean isWizard()
	{
		return wizard;
	}

	public void setWizard(boolean wizard)
	{
		this.wizard = wizard;
	}

	public TAB getTab()
	{
		return tab;
	}

	public void setTab(TAB tab)
	{
		this.tab = tab;
	}

}
