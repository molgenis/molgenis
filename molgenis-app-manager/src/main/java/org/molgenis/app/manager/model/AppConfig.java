package org.molgenis.app.manager.model;

import java.util.Map;

public class AppConfig
{
	private String label;
	private String description;
	private String version;
	private String apiDependency;
	private String name;
	private Boolean includeMenuAndFooter;
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

	public String getName()
	{
		return name;
	}

	public Boolean getIncludeMenuAndFooter()
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

	public void setName(String name)
	{
		this.name = name;
	}

	public void setIncludeMenuAndFooter(Boolean includeMenuAndFooter)
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
				+ ", " + "apiDependency=" + apiDependency + ", " + "name=" + name + ", " + "includeMenuAndFooter="
				+ includeMenuAndFooter + ", " + "runtimeOptions=" + runtimeOptions + "}";
	}
}
