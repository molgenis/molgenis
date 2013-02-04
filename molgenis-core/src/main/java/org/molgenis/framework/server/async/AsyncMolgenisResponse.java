package org.molgenis.framework.server.async;

import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.molgenis.framework.server.MolgenisContext;

public class AsyncMolgenisResponse
{
	HttpServletResponse response;

	public AsyncMolgenisResponse()
	{

	}

	public AsyncMolgenisResponse(HttpServletResponse response)
	{
		this.response = response;
	}

	public AsyncMolgenisResponse(UUID id, MolgenisContext mc)
	{
		// this.response = (HttpServletResponse)new Harry(id,mc);
	}

	public HttpServletResponse getResponse()
	{
		return response;
	}

}
