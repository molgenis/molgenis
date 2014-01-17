package org.molgenis.generators;

public class EntityMetaDataGen extends ForEachEntityGenerator
{
	public EntityMetaDataGen()
	{
	}

	@Override
	public String getDescription()
	{
		return "Generates an EntityMetaData class for each entity";
	}

	@Override
	public String getType()
	{
		return "MetaData";
	}

}
