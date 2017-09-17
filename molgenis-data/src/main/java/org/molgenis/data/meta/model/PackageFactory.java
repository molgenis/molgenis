package org.molgenis.data.meta.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class PackageFactory extends AbstractSystemEntityFactory<Package, PackageMetadata, String>
{
	PackageFactory(PackageMetadata packageMetadata, EntityPopulator entityPopulator)
	{
		super(Package.class, packageMetadata, entityPopulator);
	}

	public Package create(String id, String description)
	{
		Package package_ = create(id);
		package_.setDescription(description);
		return package_;
	}

	public Package create(String id, String description, Package parentPackage)
	{
		Package package_ = create(id);
		package_.setDescription(description);
		package_.setParent(parentPackage);
		return package_;
	}
}
