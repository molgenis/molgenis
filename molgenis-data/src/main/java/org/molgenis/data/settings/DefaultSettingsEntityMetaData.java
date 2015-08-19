package org.molgenis.data.settings;

import static org.molgenis.MolgenisFieldTypes.STRING;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.Transactional;

public abstract class DefaultSettingsEntityMetaData extends DefaultEntityMetaData
		implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	public static final String ATTR_ID = "id";

	@Autowired
	private DataService dataService;

	@Autowired
	public SettingsEntityMeta settingsEntityMeta;

	public DefaultSettingsEntityMetaData(String id)
	{
		super(id);
		setExtends(settingsEntityMeta);
		setPackage(SettingsEntityMeta.PACKAGE_SETTINGS);
		addAttribute(ATTR_ID).setIdAttribute(true).setDataType(STRING).setNillable(false).setLabel("Id")
				.setVisible(false);
	}

	@RunAsSystem
	public Entity getSettings()
	{
		return dataService.findOne(getName(), getSimpleName());
	}

	public static String getSettingsEntityName(String id)
	{
		return SettingsEntityMeta.PACKAGE_SETTINGS.getName() + '_' + id;
	}

	private Entity getDefaultSettings()
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

	@Transactional
	@RunAsSystem
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		Entity settingsEntity = getSettings();
		if (settingsEntity == null)
		{
			Entity defaultSettingsEntity = getDefaultSettings();
			defaultSettingsEntity.set(ATTR_ID, getSimpleName());
			dataService.add(getName(), defaultSettingsEntity);
		}
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE + 110;
	}
}
