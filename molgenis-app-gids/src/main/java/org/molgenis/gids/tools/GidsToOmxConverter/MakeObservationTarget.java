package org.molgenis.gids.tools.GidsToOmxConverter;

public class MakeObservationTarget
{
	private final String name;
	private final String identifier;

	public MakeObservationTarget(String name, String identifier)
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
