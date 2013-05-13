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

	private boolean enableDownloadAction;

	private boolean enableViewAction;

	private boolean enableOrderAction;

	private boolean authenticated;

	public List<JSDataSet> getDataSets()
	{
		return dataSets != null ? dataSets : Collections.<JSDataSet> emptyList();
	}

	public void setDataSets(List<JSDataSet> dataSets)
	{
		this.dataSets = dataSets;
	}

	public boolean isEnableDownloadAction()
	{
		return enableDownloadAction;
	}

	public void setEnableDownloadAction(boolean enableDownloadAction)
	{
		this.enableDownloadAction = enableDownloadAction;
	}

	public boolean isEnableViewAction()
	{
		return enableViewAction;
	}

	public void setEnableViewAction(boolean enableViewAction)
	{
		this.enableViewAction = enableViewAction;
	}

	public boolean isEnableOrderAction()
	{
		return enableOrderAction;
	}

	public void setEnableOrderAction(boolean enableOrderAction)
	{
		this.enableOrderAction = enableOrderAction;
	}

	public boolean isAuthenticated()
	{
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated)
	{
		this.authenticated = authenticated;
	}

	public boolean isVisible()
	{
		// you can use this to hide this plugin, e.g. based on user rights.
		// e.g.
		// if(!this.getLogin().hasEditPermission(myEntity)) return false;
		return true;
	}
}
