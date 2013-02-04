package org.molgenis.omicsconnect;

import org.molgenis.Molgenis;

public class OmicsConnectUpdateDatabase
{
	public static void main(String[] args) throws Exception
	{
		new Molgenis("apps/omicsconnect/org/molgenis/omicsconnect/omicsconnect.properties").updateDb(false);
	}
}