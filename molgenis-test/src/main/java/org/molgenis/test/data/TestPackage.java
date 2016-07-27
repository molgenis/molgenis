package org.molgenis.test.data;

import org.molgenis.data.meta.SystemPackage;
import org.molgenis.data.meta.model.PackageMetaData;
import org.molgenis.data.system.model.RootSystemPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class TestPackage extends SystemPackage
{
	public static final String SIMPLE_NAME = "test";
	public static final String PACKAGE_TEST_ENTITY= PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	private final RootSystemPackage rootSystemPackage;

	@Autowired
	public TestPackage(PackageMetaData packageMetaData, RootSystemPackage rootSystemPackage)
	{
		super(SIMPLE_NAME, packageMetaData);
		this.rootSystemPackage = requireNonNull(rootSystemPackage);
	}

	@Override
	protected void init()
	{
		setLabel("Test");
		setDescription("Package containing test related entities");
		setParent(rootSystemPackage);
	}
}
