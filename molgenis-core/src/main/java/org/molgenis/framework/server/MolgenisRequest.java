package org.molgenis.framework.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.molgenis.framework.db.Database;
import org.molgenis.util.tuple.HttpServletRequestTuple;

public class MolgenisRequest extends HttpServletRequestTuple
{
	private Database db;
	private String servicePath; // e.g. "/api/R/"
	private String requestPath; // e.g. "/api/R/source.R"
	private String appLocation; // e.g. "http://localhost:8080/xqtl"

	public MolgenisRequest(HttpServletRequest request) throws Exception
	{
		super(request);

	}

	public MolgenisRequest(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		super(request, response);
	}

	public String getAction()
	{
		return super.getString("__action");
	}

	public Database getDatabase()
	{
		return db;
	}

	public void setDatabase(Database db)
	{
		this.db = db;
	}

	public String getServicePath()
	{
		return servicePath;
	}

	public void setServicePath(String servicePath)
	{
		this.servicePath = servicePath;
	}

	public String getRequestPath()
	{
		return requestPath;
	}

	public void setRequestPath(String requestPath)
	{
		this.requestPath = requestPath;
	}

	public String getAppLocation()
	{
		return appLocation;
	}

	public void setAppLocation(String appLocation)
	{
		this.appLocation = appLocation;
	}

	@Override
	public String toString()
	{
		StringBuilder strBuilder = new StringBuilder();
		for (String colName : this.getColNames())
		{
			strBuilder.append(colName).append(' ');
		}
		return strBuilder.length() > 0 ? strBuilder.toString() : "NONE";
	}
}
