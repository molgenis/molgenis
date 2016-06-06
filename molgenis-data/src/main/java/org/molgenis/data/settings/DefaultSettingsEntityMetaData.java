package org.molgenis.data.settings;

import static org.molgenis.data.settings.SettingsPackage.PACKAGE_SETTINGS;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class DefaultSettingsEntityMetaData extends SystemEntityMetaData
{
	public static final String ATTR_ID = "id";

	@Autowired
	private DataService dataService;

	@Autowired
	public SettingsEntityMeta settingsEntityMeta;

	public DefaultSettingsEntityMetaData(String id)
	{
		super(id, PACKAGE_SETTINGS);
	}

	@Override
	public void init()
	{
		setExtends(settingsEntityMeta);
		setPackage(settingsEntityMeta.getPackage());
	}

	@RunAsSystem
	public Entity getSettings()
	{
		return dataService.findOneById(getName(), getSimpleName());
	}

	public static String getSettingsEntityName(String id)
	{
		return PACKAGE_SETTINGS + '_' + id;
	}

	Entity getDefaultSettings()
	{
		MapEntity mapEntity = new MapEntity(this);
		for (AttributeMetaData attr : this.getAtomicAttributes())
		{
			String defaultValue = attr.getDefaultValue();
			if (defaultValue != null)
			{
				mapEntity.set(attr.getName(), defaultValue);
			}
		}
		return mapEntity;
	}
}
