package org.molgenis.data.security.auth;

import org.molgenis.data.meta.SystemPackage;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.system.model.RootSystemPackage;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class SecurityPackage extends SystemPackage
{
	public static final String SIMPLE_NAME = "sec";
	public static final String PACKAGE_SECURITY = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	private final RootSystemPackage rootSystemPackage;

	public SecurityPackage(PackageMetadata packageMetadata, RootSystemPackage rootSystemPackage)
	{
		super(PACKAGE_SECURITY, packageMetadata);
		this.rootSystemPackage = requireNonNull(rootSystemPackage);
	}

	@Override
	protected void init()
	{
		setLabel("Security");
		setDescription("Package containing security related entities");
		setParent(rootSystemPackage);
	}
}
