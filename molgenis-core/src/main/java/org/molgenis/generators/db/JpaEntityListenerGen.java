package org.molgenis.generators.db;

import org.molgenis.generators.ForEachEntityGenerator;

public class JpaEntityListenerGen extends ForEachEntityGenerator
{
	public JpaEntityListenerGen()
	{
		super(true); // include abstract entities
	}

	@Override
	public String getDescription()
	{
		return "Generates Entity Listener Skeleton for JPA";
	}

	@Override
	public String getType()
	{
		return "EntityListener";
	}
}