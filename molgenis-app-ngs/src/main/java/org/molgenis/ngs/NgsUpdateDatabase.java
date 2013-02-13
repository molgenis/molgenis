package org.molgenis.ngs;

import org.molgenis.Molgenis;

public class NgsUpdateDatabase
{
	public static void main(String[] args) throws Exception
	{
		new Molgenis("molgenis.properties").updateDb(false);
	}
}