package org.molgenis.data.meta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RootSystemPackage extends SystemPackage
{
	public static final String PACKAGE_SYSTEM = "sys";

	@Autowired
	public RootSystemPackage(PackageMetaData packageMetaData)
	{
		super(PACKAGE_SYSTEM, packageMetaData);
	}

	@Override
	protected void init()
	{
		setLabel("System");
		setDescription("Package containing all system entities");
	}
}
