package org.molgenis.tifn;

import org.molgenis.Molgenis;

public class TifnUpdateDatabase
{
	public static void main(String[] args) throws Exception
	{
		new Molgenis("molgenis.properties").updateDb(false);
	}
}