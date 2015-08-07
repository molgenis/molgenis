package org.molgenis.gaf.settings;

import org.molgenis.data.Entity;
import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.gaf.GafListImporterController;
import org.springframework.stereotype.Component;

@Component
public class GafListSettings extends DefaultSettingsEntity
{
	private static final long serialVersionUID = 1L;

	private static final String ID = GafListImporterController.ID;

	public GafListSettings()
	{
		super(ID);
	}

	public String getEntityName()
	{
		return getString(Meta.ENTITY_NAME);
	}

	@Component
	private static class Meta extends DefaultSettingsEntityMetaData
	{
		public static final String ENTITY_NAME = "entity_name";

		public Meta()
		{
			super(ID);
			setLabel("GAF list settings");
			setDescription("Settings for the GAF List Importer plugin.");

			addAttribute(ENTITY_NAME).setNillable(true).setLabel("Entity name")
					.setDescription("Data set to which imported files are added");
		}

		@Override
		protected Entity getDefaultSettings()
		{
			// FIXME workaround for https://github.com/molgenis/molgenis/issues/1810
			MapEntity defaultSettings = new MapEntity(this);
			return defaultSettings;
		}
	}
}
