package org.molgenis.dataexplorer.settings;

import org.molgenis.data.settings.SettingsEntityMeta;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class EntityReportMeta extends DefaultEntityMetaData
{
	public EntityReportMeta()
	{
		super(EntityReport.ENTITY_NAME, EntityReport.class);
		setPackage(SettingsEntityMeta.PACKAGE_SETTINGS);

		addAttribute(EntityReport.ENTITY).setIdAttribute(true).setNillable(false).setLabel("Entity");
		addAttribute(EntityReport.REPORT).setNillable(false).setLabel("Report");
	}
}