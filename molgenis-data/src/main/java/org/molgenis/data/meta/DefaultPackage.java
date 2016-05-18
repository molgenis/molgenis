package org.molgenis.data.meta;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.settings.SettingsEntityMeta;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultPackage extends Package
{
	@Autowired
	public DefaultPackage(PackageMetaData packageMetaData) {
		super(new MapEntity(requireNonNull(packageMetaData)));
		setName(SettingsEntityMeta.PACKAGE_NAME);
		setDescription("Application and plugin settings");
	}
}
