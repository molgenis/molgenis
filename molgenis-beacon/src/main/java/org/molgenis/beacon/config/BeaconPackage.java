package org.molgenis.beacon.config;

import org.molgenis.data.meta.SystemPackage;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.system.model.RootSystemPackage;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class BeaconPackage extends SystemPackage
{
	private static final String SIMPLE_NAME = "beacons";
	private final RootSystemPackage rootSystemPackage;

	public static final String PACKAGE_BEACON = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public BeaconPackage(PackageMetadata packageMetadata, RootSystemPackage rootSystemPackage)
	{
		super(PACKAGE_BEACON, packageMetadata);
		this.rootSystemPackage = requireNonNull(rootSystemPackage);
	}

	@Override
	protected void init()
	{
		setLabel("Beacons");
		setParent(rootSystemPackage);
	}
}
