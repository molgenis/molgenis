package org.molgenis.omicsconnect;

import org.molgenis.Molgenis;

public class OmicsConnectUpdateDatabase
{
	public static void main(String[] args) throws Exception
	{
		new Molgenis("molgenis.properties").updateDb(false);
	}
}