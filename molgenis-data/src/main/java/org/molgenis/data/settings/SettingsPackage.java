package org.molgenis.data.settings;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.meta.Package;
import org.molgenis.data.meta.PackageMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SettingsPackage extends Package
{
	@Autowired
	public SettingsPackage(PackageMetaData packageMetaData) {
		super(new MapEntity(requireNonNull(packageMetaData)));
		setName(SettingsEntityMeta.PACKAGE_NAME);
		setDescription("Application and plugin settings");
	}
}
