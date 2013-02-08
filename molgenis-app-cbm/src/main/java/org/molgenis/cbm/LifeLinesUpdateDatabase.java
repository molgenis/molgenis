package org.molgenis.lifelines;

import org.molgenis.Molgenis;

public class LifeLinesUpdateDatabase
{
	public static void main(String[] args) throws Exception
	{
		new Molgenis("molgenis.properties").updateDb(false);
	}
}