package org.molgenis.framework.server;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.molgenis.MolgenisOptions;

public class MolgenisContext
{
	private final ServletConfig sc;
	private MolgenisOptions usedOptions;
	private final String variant;
	private final TokenFactory tokenFactory;

	public MolgenisContext(ServletConfig sc, MolgenisOptions usedOptions, String variant)
	{
		this.sc = sc;
		this.usedOptions = usedOptions;
		this.variant = variant;
		this.tokenFactory = new TokenFactory();
	}

	public TokenFactory getTokenFactory()
	{
		return tokenFactory;
	}

	public String getVariant()
	{
		return variant;
	}

	public ServletConfig getServletConfig()
	{
		return sc;
	}

	public ServletContext getServletContext()
	{
		return sc.getServletContext();
	}

	public MolgenisOptions getUsedOptions()
	{
		return usedOptions;
	}

	public void setUsedOptions(MolgenisOptions usedOptions)
	{
		this.usedOptions = usedOptions;
	}
}
