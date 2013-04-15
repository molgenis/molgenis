package org.molgenis.omx.auth.service;

import java.io.IOException;

import javax.servlet.ServletException;

import nl.captcha.servlet.SimpleCaptchaServlet;

import org.molgenis.framework.server.MolgenisContext;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.server.MolgenisResponse;
import org.molgenis.framework.server.MolgenisService;

public class MolgenisCaptchaService extends SimpleCaptchaServlet implements MolgenisService
{

	private static final long serialVersionUID = -7806912993766663119L;

	private MolgenisContext mc;

	public MolgenisCaptchaService(MolgenisContext mc)
	{
		this.mc = mc;
	}

	@Override
	public void handleRequest(MolgenisRequest request, MolgenisResponse response) throws IOException
	{

		try
		{
			super.service(request.getRequest(), response.getResponse());
		}
		catch (ServletException e)
		{
			throw new IOException(e);
		}

	}

}