package org.molgenis.omx.harmonization.plugin;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.omx.observ.DataSet;

public class HarmonizationModel
{
	List<DataSet> dataSets = new ArrayList<DataSet>();

	public List<DataSet> getDataSets()
	{
		return dataSets;
	}

	public void setDataSets(List<DataSet> dataSets)
	{
		this.dataSets = dataSets;
	}
}