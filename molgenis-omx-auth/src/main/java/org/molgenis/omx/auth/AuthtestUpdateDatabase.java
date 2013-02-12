package org.molgenis.omx.auth;

import org.molgenis.Molgenis;

//import cmdline.CmdLineException;

public class AuthtestUpdateDatabase
{
	public static void main(String[] args) throws Exception
	{
		new Molgenis("handwritten/apps/org/molgenis/authtest/authtest.properties").updateDb(true);
	}
}
