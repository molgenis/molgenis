package org.molgenis.script.core;

import org.molgenis.data.meta.SystemPackage;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.system.model.RootSystemPackage;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class ScriptPackage extends SystemPackage
{
	public static final String SIMPLE_NAME = "scr";
	public static final String PACKAGE_SCRIPT = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	private final RootSystemPackage rootSystemPackage;

	public ScriptPackage(PackageMetadata packageMetadata, RootSystemPackage rootSystemPackage)
	{
		super(PACKAGE_SCRIPT, packageMetadata);
		this.rootSystemPackage = requireNonNull(rootSystemPackage);
	}

	@Override
	protected void init()
	{
		setLabel("Script");
		setParent(rootSystemPackage);
	}
}
