package org.molgenis.data.meta.model;

import org.molgenis.data.meta.SystemPackage;
import org.molgenis.data.system.model.RootSystemPackage;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class MetaPackage extends SystemPackage
{
	private static final String SIMPLE_NAME = "md";
	public static final String PACKAGE_META = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	private final RootSystemPackage rootSystemPackage;

	public MetaPackage(PackageMetadata packageMetadata, RootSystemPackage rootSystemPackage)
	{
		super(PACKAGE_META, packageMetadata);
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
