package org.molgenis.app.manager.model;

import java.util.Map;

public class AppConfig
{
	private String label;
	private String description;
	private String version;
	private String apiDependency;
	private String uri;
	private boolean includeMenuAndFooter;
	private Map<String, Object> runtimeOptions;

	public String getLabel()
	{
		return label;
	}

	public String getDescription()
	{
		return description;
	}

	public String getVersion()
	{
		return version;
	}

	public String getApiDependency()
	{
		return apiDependency;
	}

	public String getUri()
	{
		return uri;
	}

	public boolean getIncludeMenuAndFooter()
	{
		return includeMenuAndFooter;
	}

	public Map<String, Object> getRuntimeOptions()
	{
		return runtimeOptions;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public void setApiDependency(String apiDependency)
	{
		this.apiDependency = apiDependency;
	}

	public void setUri(String uri)
	{
		this.uri = uri;
	}

	public void setIncludeMenuAndFooter(boolean includeMenuAndFooter)
	{
		this.includeMenuAndFooter = includeMenuAndFooter;
	}

	public void setRuntimeOptions(Map<String, Object> runtimeOptions)
	{
		this.runtimeOptions = runtimeOptions;
	}

	public String toString()
	{
		return "AppConfig{" + "label=" + label + ", " + "description=" + description + ", " + "version=" + version
				+ ", " + "apiDependency=" + apiDependency + ", " + "uri=" + uri + ", " + "includeMenuAndFooter="
				+ includeMenuAndFooter + ", " + "runtimeOptions=" + runtimeOptions + "}";
	}
}
