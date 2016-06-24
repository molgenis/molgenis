package org.molgenis.data.meta;

import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetaData;
import org.molgenis.data.support.BootstrapEntity;

public abstract class SystemPackage extends Package
{
	protected SystemPackage(String packageName, PackageMetaData packageMetaData)
	{
		super(new BootstrapEntity(packageMetaData));
		setSimpleName(packageName);
	}

	public void bootstrap()
	{
		init();
	}

	protected abstract void init();
}
