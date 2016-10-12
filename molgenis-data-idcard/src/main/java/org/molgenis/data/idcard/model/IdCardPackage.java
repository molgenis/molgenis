package org.molgenis.data.idcard.model;

import org.molgenis.data.meta.SystemPackage;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.system.model.RootSystemPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class IdCardPackage extends SystemPackage
{
	public static final String SIMPLE_NAME = "idc";
	public static final String PACKAGE_ID_CARD = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	private final RootSystemPackage rootSystemPackage;

	@Autowired
	public IdCardPackage(PackageMetadata packageMetadata, RootSystemPackage rootSystemPackage)
	{
		super(SIMPLE_NAME, packageMetadata);
		this.rootSystemPackage = requireNonNull(rootSystemPackage);
	}

	@Override
	protected void init()
	{
		setLabel("ID-Card");
		setParent(rootSystemPackage);
	}
}


