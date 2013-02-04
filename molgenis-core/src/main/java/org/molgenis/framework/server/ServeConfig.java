package org.molgenis.framework.server;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class ServeConfig implements ServletConfig
{

	private ServletContext context;

	private Hashtable<String, Object> init_params;

	private String servletName;

	public ServeConfig(ServletContext context)
	{
		this(context, null, "undefined");
	}

	public ServeConfig(ServletContext context, Hashtable<String, Object> initParams, String servletName)
	{
		this.context = context;
		this.init_params = initParams;
		this.servletName = servletName;
	}

	// Methods from ServletConfig.

	// / Returns the context for the servlet.
	@Override
	public ServletContext getServletContext()
	{
		return context;
	}

	// / Gets an initialization parameter of the servlet.
	// @param name the parameter name
	@Override
	public String getInitParameter(String name)
	{
		// This server supports servlet init params. :)
		if (init_params != null) return (String) init_params.get(name);
		return null;
	}

	// / Gets the names of the initialization parameters of the servlet.
	// @param name the parameter name
	@Override
	public Enumeration<String> getInitParameterNames()
	{
		// This server does:) support servlet init params.
		if (init_params != null) return init_params.keys();
		return new Vector<String>().elements();
	}

	// 2.2
	@Override
	public String getServletName()
	{
		return servletName;
	}

}
