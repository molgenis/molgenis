package org.molgenis.omx.study;

import java.util.List;

/**
 * Study definition meta data
 * 
 * @author erwin
 */
public class StudyDefinitionInfo
{
	private final String id;
	private final String name;
	private String description;
	private String version;
	private List<String> authors;

	public StudyDefinitionInfo(String id, String name)
	{
		if (id == null) throw new IllegalArgumentException("Id is null");
		if (name == null) throw new IllegalArgumentException("Name is null");
		this.id = id;
		this.name = name;
	}

	public String getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public List<String> getAuthors()
	{
		return authors;
	}

	public void setAuthors(List<String> authors)
	{
		this.authors = authors;
	}
}
