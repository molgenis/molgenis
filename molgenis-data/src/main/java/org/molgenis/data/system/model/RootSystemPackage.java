package org.molgenis.data.system.model;

import org.molgenis.data.meta.SystemPackage;
import org.molgenis.data.meta.model.PackageMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RootSystemPackage extends SystemPackage
{
	public static final String PACKAGE_SYSTEM = "sys";

	@Autowired
	public RootSystemPackage(PackageMetadata packageMetadata)
	{
		super(PACKAGE_SYSTEM, packageMetadata);
	}

	@Override
	protected void init()
	{
		setLabel("System");
		setDescription("Package containing all system entities");
	}
}
