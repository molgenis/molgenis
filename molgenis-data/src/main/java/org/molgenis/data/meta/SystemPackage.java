package org.molgenis.data.meta;

import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.support.BootstrapEntity;

public abstract class SystemPackage extends Package
{
	protected SystemPackage(String packageId, PackageMetadata packageMetadata)
	{
		super(new BootstrapEntity(packageMetadata));
		setId(packageId);
	}

	public void bootstrap()
	{
		init();
	}

	protected abstract void init();
}