package org.molgenis.tools.gids.convertor;

public class MakeEntityNameAndIdentifier
{
	private final String name;
	private final String identifier;
	private final String features_identifier;

	public MakeEntityNameAndIdentifier(String name, String identifier)
	{
		this.identifier = identifier;
		this.name = name;
		this.features_identifier = null;
	}

	public MakeEntityNameAndIdentifier(String name, String identifier, String features_identifier)
	{
		this.identifier = identifier;
		this.name = name;
		this.features_identifier = features_identifier;
	}

	public String getName()
	{
		return name;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public String getFeatures_Identifier()
	{
		return features_identifier;
	}

}
