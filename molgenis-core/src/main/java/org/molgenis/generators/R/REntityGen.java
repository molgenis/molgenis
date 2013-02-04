package org.molgenis.generators.R;

import org.molgenis.generators.ForEachEntityGenerator;

public class REntityGen extends ForEachEntityGenerator
{
	public REntityGen()
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
	public String getType()
	{
		return "";
	}

	@Override
	// change default .java.ftl to .R.ftl
	public String getExtension()
	{
		return ".R";
	}

}
