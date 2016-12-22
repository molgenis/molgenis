package org.molgenis.data.meta;

import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.support.BootstrapEntity;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.Objects.requireNonNull;

public abstract class SystemPackage extends Package
{
	private IdGenerator idGenerator;

	protected SystemPackage(String packageName, PackageMetadata packageMetadata)
	{
		super(new BootstrapEntity(packageMetadata));
		setSimpleName(packageName);
	}

	public void bootstrap()
	{
		setId(idGenerator.generateId());
		init();
	}

	protected abstract void init();

	@Autowired
	public void setIdGenerator(IdGenerator idGenerator)
	{
		this.idGenerator = requireNonNull(idGenerator);
	}
}
