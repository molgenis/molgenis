package org.molgenis.framework.server;

import javax.servlet.http.HttpServletResponse;

public class MolgenisResponse
{
	HttpServletResponse response;

	public MolgenisResponse(HttpServletResponse response)
	{
		this.response = response;
	}

	public HttpServletResponse getResponse()
	{
		return response;
	}

}
