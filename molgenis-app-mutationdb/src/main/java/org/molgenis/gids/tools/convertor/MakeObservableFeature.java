package org.molgenis.gids.tools.convertor;

public class MakeObservableFeature
{
	private final String name;
	private final String identifier;
	private final String datetype;
	private final String description;

	public MakeObservableFeature(String name, String identifier, String description, String datetype)
	{
		this.identifier = identifier;
		this.name = name;
		this.datetype = datetype;
		this.description = description;
	}

	public String getName()
	{
		return name;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public String getDescription()
	{
		return description;
	}

	public String getDateType()
	{

		return datetype;
	}

}
