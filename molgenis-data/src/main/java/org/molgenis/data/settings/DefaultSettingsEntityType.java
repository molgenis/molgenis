package org.molgenis.data.settings;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;

import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.settings.SettingsPackage.PACKAGE_SETTINGS;
import static org.molgenis.util.EntityUtils.getTypedValue;

public abstract class DefaultSettingsEntityType extends SystemEntityType
{
	public static final String ATTR_ID = "id";
	private final String id;

	@Autowired
	private DataService dataService;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	public SettingsEntityType settingsEntityType;

	@Autowired
	private SettingsPackage settingsPackage;

	public DefaultSettingsEntityType(String id)
	{
		super(id, PACKAGE_SETTINGS);
		this.id = id;
	}

	@Override
	public void init()
	{
		setExtends(settingsEntityType);
		setPackage(settingsPackage);
	}

	@RunAsSystem
	public Entity getSettings()
	{
		return dataService.findOneById(getId(), getSettingsEntityId());
	}

	public static String getSettingsEntityName(String id)
	{
		return PACKAGE_SETTINGS + PACKAGE_SEPARATOR + id;
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
		defaultSettingsEntity.set(ATTR_ID, getSettingsEntityId());
		return defaultSettingsEntity;
	}

	private String getSettingsEntityId()
	{
		return getId().substring(PACKAGE_SETTINGS.length() + PACKAGE_SEPARATOR.length());
	}
}
