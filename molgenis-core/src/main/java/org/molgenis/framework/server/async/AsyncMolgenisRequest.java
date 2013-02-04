package org.molgenis.framework.server.async;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.molgenis.framework.server.MolgenisRequest;

public class AsyncMolgenisRequest extends MolgenisRequest
{

	UUID loadingScreenId;

	public AsyncMolgenisRequest(HttpServletRequest request) throws Exception
	{
		super(request);

	}

	public AsyncMolgenisRequest(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		super(request, response);

	}

	public UUID getLoadingScreenId()
	{
		return loadingScreenId;
	}

	public void setLoadingScreenId(UUID loadingScreenId)
	{
		this.loadingScreenId = loadingScreenId;
	}

}
