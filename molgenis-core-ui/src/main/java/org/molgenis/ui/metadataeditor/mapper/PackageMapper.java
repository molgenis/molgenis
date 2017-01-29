package org.molgenis.ui.metadataeditor.mapper;

import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.support.LazyEntity;
import org.molgenis.ui.metadataeditor.model.EditorPackageIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
class PackageMapper
{
	private final PackageFactory packageFactory;
	private final DataService dataService;

	@Autowired
	PackageMapper(PackageFactory packageFactory, DataService dataService)
	{
		this.packageFactory = requireNonNull(packageFactory);
		this.dataService = requireNonNull(dataService);
	}

	EditorPackageIdentifier toEditorPackage(Package package_)
	{
		if (package_ == null)
		{
			return null;
		}
		return EditorPackageIdentifier.create(package_.getName(), package_.getLabel());
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
