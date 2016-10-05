package org.molgenis.data.settings;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;

import static org.molgenis.data.settings.SettingsPackage.PACKAGE_SETTINGS;
import static org.molgenis.util.EntityUtils.getTypedValue;

public abstract class DefaultSettingsEntityMetaData extends SystemEntityMetaData
{
	public static final String ATTR_ID = "id";

	@Autowired
	private DataService dataService;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	public SettingsEntityMeta settingsEntityMeta;

	@Autowired
	private SettingsPackage settingsPackage;

	public DefaultSettingsEntityMetaData(String id)
	{
		super(id, PACKAGE_SETTINGS);
	}

	@Override
	public void init()
	{
		setExtends(settingsEntityMeta);
		setPackage(settingsPackage);
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
		Entity defaultSettingsEntity = new DynamicEntity(this);
		for (Attribute attr : this.getAtomicAttributes())
		{
			// default values are stored/retrieved as strings, so we convert them to the required type here.
			String defaultValue = attr.getDefaultValue();
			if (defaultValue != null)
			{
				Object typedDefaultValue = getTypedValue(defaultValue, attr, entityManager);
				defaultSettingsEntity.set(attr.getName(), typedDefaultValue);
			}
		}
		return defaultSettingsEntity;
	}
}
