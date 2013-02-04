package org.molgenis.generators.db;

import org.molgenis.generators.ForEachEntityGenerator;


public class EntityImporterGen extends ForEachEntityGenerator
{
	@Override
	public String getDescription()
	{
		return "Generates importer for each entity";
	}

	@Override
	public Boolean skipSystem()
	{
		return false; // TODO true?
	}
}
