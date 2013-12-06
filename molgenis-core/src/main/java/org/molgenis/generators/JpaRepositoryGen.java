package org.molgenis.generators;

public class JpaRepositoryGen extends ForEachEntityGenerator
{

	public JpaRepositoryGen()
	{
	}

	@Override
	public String getDescription()
	{
		return "Generates a jpa repository interfaces for every entity";
	}

	@Override
	public String getType()
	{
		return "Repository";
	}

}
