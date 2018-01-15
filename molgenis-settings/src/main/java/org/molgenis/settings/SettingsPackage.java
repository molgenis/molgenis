package org.molgenis.settings;

import org.molgenis.data.meta.SystemPackage;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.system.model.RootSystemPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class SettingsPackage extends SystemPackage
{
	public static final String SIMPLE_NAME = "set";
	public static final String PACKAGE_SETTINGS = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	private RootSystemPackage rootSystemPackage;

	public SettingsPackage(PackageMetadata packageMetadata)
	{
		super(PACKAGE_SETTINGS, packageMetadata);
	}

	@Override
	protected void init()
	{
		setLabel("Settings");
		setDescription("Application and plugin settings");
		setParent(rootSystemPackage);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setRootSystemPackage(RootSystemPackage rootSystemPackage)
	{
		this.rootSystemPackage = requireNonNull(rootSystemPackage);
	}
}
