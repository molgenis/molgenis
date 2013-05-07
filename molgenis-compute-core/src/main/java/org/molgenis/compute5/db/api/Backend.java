package org.molgenis.compute5.db.api;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Backend
{

	private final String name;
	private final String url;
	private final String type;
	private final String command;

	public Backend(String name, String url, String type, String command)
	{
		this.name = name;
		this.url = url;
		this.type = type;
		this.command = command;
	}

	public String getName()
	{
		return name;
	}

	public String getUrl()
	{
		return url;
	}

	public String getType()
	{
		return type;
	}

	public String getCommand()
	{
		return command;
	}

	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this);
	}
}
