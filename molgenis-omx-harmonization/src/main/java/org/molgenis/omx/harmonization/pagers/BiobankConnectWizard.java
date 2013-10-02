package org.molgenis.omx.harmonization.pagers;

import java.util.List;

import org.molgenis.omx.observ.DataSet;
import org.molgenis.ui.wizard.Wizard;

public class BiobankConnectWizard extends Wizard
{
	private static final long serialVersionUID = 1L;

	private List<DataSet> dataSets;

	private DataSet selectedDataSet;

	public List<DataSet> getDataSets()
	{
		return dataSets;
	}

	public void setDataSets(List<DataSet> dataSets)
	{
		this.dataSets = dataSets;
	}

	public DataSet getSelectedDataSet()
	{
		return selectedDataSet;
	}

	public void setSelectedDataSet(DataSet selectedDataSet)
	{
		this.selectedDataSet = selectedDataSet;
	}
}