package org.molgenis.data.cache.l2.settings;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class L2CacheSettingsFactory
		extends AbstractSystemEntityFactory<L2CacheSettings, L2CacheSettingsMetaData, String>
{
	@Autowired
	L2CacheSettingsFactory(L2CacheSettingsMetaData l2CacheSettingsMetaData)
	{
		super(L2CacheSettings.class, l2CacheSettingsMetaData);
	}
}
