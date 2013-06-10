package org.molgenis.generators;

import org.molgenis.MolgenisOptions;
import org.molgenis.model.elements.Model;

public class DataTypeGen extends ForEachEntityGenerator
{
	public DataTypeGen()
	{
		// include abstract entities
		super(true);
	}

	@Override
	public void generate(Model model, MolgenisOptions options) throws Exception
	{
		if (options.generate_tests)
		{
			super.setIncludeAbstract(false);
			generate(model, options, true);
		}
		else
		{
			generate(model, options, false);
		}
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
