package org.molgenis.data.meta;

import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.support.BootstrapEntity;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.Objects.requireNonNull;

public abstract class SystemPackage extends Package
{
	private String packageName;

	protected SystemPackage(String packageName, PackageMetadata packageMetadata)
	{
		super(new BootstrapEntity(packageMetadata));
		setName(packageName);
	}

	public void bootstrap()
	{
		init();
		setId(MetaUtils.getFullyQualyfiedName(packageName, getParent()));
	}

	protected abstract void init();

	public Package setName(String name)
	{
		this.packageName = name;
		super.setName(name);
		return this;
	}
}