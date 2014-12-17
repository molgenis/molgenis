package org.molgenis.compute.ui.meta;

import org.molgenis.data.meta.PackageImpl;

public class ComputeUiPackage extends PackageImpl
{
	public static final ComputeUiPackage INSTANCE = new ComputeUiPackage();

	private ComputeUiPackage()
	{
		super("computeui", "Compute UI package");
	}
}
