package org.molgenis.generators.ui;

import org.molgenis.generators.ForEachEntityGenerator;

public class HtmlFormGen extends ForEachEntityGenerator
{
	@Override
	public String getDescription()
	{
		return "Generates html forms for each entity.";
	}

	@Override
	public String getType()
	{
		return "Form";
	}
}
