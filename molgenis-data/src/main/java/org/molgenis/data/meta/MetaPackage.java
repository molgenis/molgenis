package org.molgenis.data.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.RootSystemPackage.PACKAGE_SYSTEM;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MetaPackage extends SystemPackage
{
	private static final String SIMPLE_NAME = "md";
	public static final String PACKAGE_META = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	private final RootSystemPackage rootSystemPackage;

	@Autowired
	public MetaPackage(PackageMetaData packageMetaData, RootSystemPackage rootSystemPackage)
	{
		super(SIMPLE_NAME, packageMetaData);
		this.rootSystemPackage = requireNonNull(rootSystemPackage);
	}

	@Override
	protected void init()
	{
		setLabel("Meta");
		setDescription("Package containing all meta data entities");
		setParent(rootSystemPackage);
	}
}
