package org.molgenis.omx.plugins;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.molgenis.omx.plugins.ProtocolViewerController.JSDataSet;

/**
 * Protocol viewer model
 */
public class ProtocolViewer implements Serializable
{
	private static final long serialVersionUID = 1L;

	private List<JSDataSet> dataSets;

	public List<JSDataSet> getDataSets()
	{
		return dataSets != null ? dataSets : Collections.<JSDataSet> emptyList();
	}

	public void setDataSets(List<JSDataSet> dataSets)
	{
		this.dataSets = dataSets;
	}
}
