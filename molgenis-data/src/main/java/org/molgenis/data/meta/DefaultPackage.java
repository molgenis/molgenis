package org.molgenis.data.meta;

import org.molgenis.data.meta.model.PackageMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultPackage extends SystemPackage
{
	public static final String PACKAGE_DEFAULT = "base";

	@Autowired
	public DefaultPackage(PackageMetaData packageMetaData)
	{
		super(PACKAGE_DEFAULT, packageMetaData);
	}

	@Override
	protected void init()
	{
		setLabel("Default");
		setDescription("Default packages for new entities");
	}
}
