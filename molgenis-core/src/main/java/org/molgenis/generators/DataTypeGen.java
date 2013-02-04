package org.molgenis.generators;

public class DataTypeGen extends ForEachEntityGenerator
{
	public DataTypeGen()
	{
		// include abstract entities
		super(true);
	}

	@Override
	public String getDescription()
	{
		return "Generates classes for each entity (simple 'bean's or 'pojo's).";
	}

	@Override
	public String getType()
	{
		return "";
	}

}
