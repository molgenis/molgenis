package org.molgenis.semanticmapper.meta;

import org.molgenis.data.meta.SystemPackage;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.system.model.RootSystemPackage;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class MapperPackage extends SystemPackage
{
	public static final String SIMPLE_NAME = "map";
	public static final String PACKAGE_MAPPER = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	private final RootSystemPackage rootSystemPackage;

	public MapperPackage(PackageMetadata packageMetadata, RootSystemPackage rootSystemPackage)
	{
		super(PACKAGE_MAPPER, packageMetadata);
		this.rootSystemPackage = requireNonNull(rootSystemPackage);
	}

	@Override
	protected void init()
	{
		setLabel("Mapper");
		setParent(rootSystemPackage);
	}
}

