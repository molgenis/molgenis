package org.molgenis.omx.auth;

import org.molgenis.Molgenis;

/**
 * Generates the MOLGENIS application from the *db.xml and *ui.xml as set in
 * molgenis.properties
 */
public class AuthtestGenerate
{
	public static void main(String[] args) throws Exception
	{
		new Molgenis("handwritten/apps/org/molgenis/authtest/authtest.properties").generate();
	}
}
