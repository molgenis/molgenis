package org.molgenis.data.meta;

import org.molgenis.data.meta.model.PackageMetadata;
import org.springframework.stereotype.Component;

@Component
public class DefaultPackage extends SystemPackage
{
	public static final String PACKAGE_DEFAULT = "base";

	public DefaultPackage(PackageMetadata packageMetadata)
	{
		super(PACKAGE_DEFAULT, packageMetadata);
	}

	@Override
	protected void init()
	{
		setLabel("Default");
		setDescription("Default packages for new entities");
	}
}
