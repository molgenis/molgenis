package org.molgenis.generators.db;

import org.molgenis.MolgenisOptions;
import org.molgenis.generators.ForEachEntityGenerator;
import org.molgenis.model.elements.Model;

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

	@Override
	public void generate(Model model, MolgenisOptions options) throws Exception
	{
		if (options.generate_tests) generate(model, options, true);
		else generate(model, options, false);
	}
}
