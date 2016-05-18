package org.molgenis.data.settings;

import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.molgenis.data.support.MapEntity;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.Transactional;

public abstract class DefaultSettingsEntityMetaData extends SystemEntityMetaDataImpl
{
	public static final String ATTR_ID = "id";

	@Autowired
	private DataService dataService;

	@Autowired
	public SettingsEntityMeta settingsEntityMeta;

	public DefaultSettingsEntityMetaData(String id)
	{
		super(id);
	}

	@Override
	public void init()
	{
		setExtends(settingsEntityMeta);
		setPackage(settingsEntityMeta.getPackage());
		addAttribute(ATTR_ID, ROLE_ID).setLabel("Id").setVisible(false);
	}

	@RunAsSystem
	public Entity getSettings()
	{
		return dataService.findOneById(getName(), getSimpleName());
	}

	public static String getSettingsEntityName(String id)
	{
		return SettingsEntityMeta.PACKAGE_NAME + '_' + id;
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
