package org.molgenis.metadata.manager.mapper;

import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.support.LazyEntity;
import org.molgenis.metadata.manager.model.EditorPackageIdentifier;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class PackageMapper
{
	private final PackageFactory packageFactory;
	private final DataService dataService;

	PackageMapper(PackageFactory packageFactory, DataService dataService)
	{
		this.packageFactory = requireNonNull(packageFactory);
		this.dataService = requireNonNull(dataService);
	}

	public EditorPackageIdentifier toEditorPackage(Package package_)
	{
		if (package_ == null)
		{
			return null;
		}
		return EditorPackageIdentifier.create(package_.getId(), package_.getLabel());
	}

	Package toPackageReference(EditorPackageIdentifier editorPackageIdentifier)
	{
		if (editorPackageIdentifier == null)
		{
			return null;
		}
		return new Package(
				new LazyEntity(packageFactory.getEntityType(), dataService, editorPackageIdentifier.getId()));
	}
}
