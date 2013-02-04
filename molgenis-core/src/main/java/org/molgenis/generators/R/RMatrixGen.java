package org.molgenis.generators.R;

import org.molgenis.generators.ForEachMatrixGenerator;

public class RMatrixGen extends ForEachMatrixGenerator
{
	public RMatrixGen()
	{
		// include abstract entities: no
		super(false);
	}

	@Override
	public String getDescription()
	{
		return "Generates access methods in R for each matrix.";
	}

	@Override
	public String getType()
	{
		// will be the name of the matrix.
		return "";
	}

	@Override
	// change default .java.ftl to .R.ftl
	public String getExtension()
	{
		return ".R";
	}
}
