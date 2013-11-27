package org.molgenis.omx.protocolviewer;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.Protocol;

/**
 * Protocol viewer model
 */
public class ProtocolViewer implements Serializable
{
	private static final long serialVersionUID = 1L;

	private List<Protocol> protocols;

	private boolean enableDownloadAction;

	private boolean enableOrderAction;

	private boolean authenticated;

	public List<Protocol> getProtocols()
	{
		return protocols != null ? protocols : Collections.<Protocol> emptyList();
	}

	public void setProtocols(List<Protocol> protocols)
	{
		this.protocols = protocols;
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
