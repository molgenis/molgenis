package org.molgenis.generators.db;

import org.molgenis.generators.ForEachEntityGenerator;

public class JpaMapperGen extends ForEachEntityGenerator
{
	@Override
	public String getDescription()
	{
		return "Generates database mappers for each entity using JPA.";
	}

	@Override
	public String getType()
	{
		return "JpaMapper";
	}
}
