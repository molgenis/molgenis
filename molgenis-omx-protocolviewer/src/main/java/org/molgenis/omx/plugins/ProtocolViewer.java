package org.molgenis.omx.plugins;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.molgenis.omx.observ.DataSet;

/**
 * Protocol viewer model
 */
public class ProtocolViewer implements Serializable
{
	private static final long serialVersionUID = 1L;

	private List<DataSet> dataSets;

	private boolean enableDownloadAction;

	private boolean enableOrderAction;

	private boolean authenticated;

	public List<DataSet> getDataSets()
	{
		return dataSets != null ? dataSets : Collections.<DataSet> emptyList();
	}

	public void setDataSets(List<DataSet> dataSets)
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
}
