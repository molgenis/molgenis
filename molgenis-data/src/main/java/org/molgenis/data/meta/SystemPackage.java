package org.molgenis.data.meta;

public abstract class SystemPackage extends Package
{
	protected SystemPackage(String packageName, PackageMetaData packageMetaData)
	{
		super(packageName, packageMetaData);
	}

	public void bootstrap()
	{
		init();
	}

	protected abstract void init();
}
