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

	private boolean showViewButton;

	private boolean showOrderButton;

	private boolean authenticated;

	public List<JSDataSet> getDataSets()
	{
		return dataSets != null ? dataSets : Collections.<JSDataSet> emptyList();
	}

	public void setDataSets(List<JSDataSet> dataSets)
	{
		this.dataSets = dataSets;
	}

	public boolean getShowViewButton()
	{
		return this.showViewButton;
	}

	public void setShowViewButton(boolean showViewButton)
	{
		this.showViewButton = showViewButton;
	}

	public boolean getShowOrderButton()
	{
		return this.showOrderButton;
	}

	public void setShowOrderButton(boolean showOrderButton)
	{
		this.showOrderButton = showOrderButton;
	}

	public boolean isAuthenticated()
	{
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated)
	{
		this.authenticated = authenticated;
	}
}
