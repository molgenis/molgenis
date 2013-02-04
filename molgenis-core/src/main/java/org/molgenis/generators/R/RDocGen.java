package org.molgenis.generators.R;

import org.molgenis.generators.ForEachEntityGenerator;

public class RDocGen extends ForEachEntityGenerator
{
	public RDocGen()
	{
		// include abstract entities: no
		super(false);
	}

	@Override
	public String getDescription()
	{
		return "Generates access methods in R for each (concrete) entity.";
	}

	@Override
	// change default .java.ftl to .R.ftl
	public String getExtension()
	{
		return ".Rd";
	}
}
