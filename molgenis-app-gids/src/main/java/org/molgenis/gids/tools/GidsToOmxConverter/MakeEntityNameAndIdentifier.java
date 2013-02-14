package org.molgenis.gids.tools.GidsToOmxConverter;

public class MakeEntityNameAndIdentifier
{
	private final String name;
	private final String identifier;

	public MakeEntityNameAndIdentifier(String name, String identifier)
	{
		this.identifier = identifier;
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public String getIdentifier()
	{
		return identifier;
	}

}
