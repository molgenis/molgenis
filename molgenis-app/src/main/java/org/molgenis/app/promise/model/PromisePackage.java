package org.molgenis.app.promise.model;

import org.molgenis.data.Package;
import org.molgenis.data.meta.PackageImpl;

public class PromisePackage
{
	private static Package PROMISE_PACKAGE;
	public static final String NAME = "promise";
	public static final String DESCRIPTION = "ProMISe";

	private PromisePackage()
	{

	}

	public static Package getPackage()
	{
		if (PROMISE_PACKAGE == null)
		{
			PROMISE_PACKAGE = new PackageImpl(NAME, DESCRIPTION);
		}
		return PROMISE_PACKAGE;
	}
}
