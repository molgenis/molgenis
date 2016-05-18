package org.molgenis.data.meta.system;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum SystemEntityMetaDataRegistry
{
	INSTANCE; // http://stackoverflow.com/a/71399

	private final Logger LOG = LoggerFactory.getLogger(SystemEntityMetaDataRegistry.class);

	private final Map<String, SystemEntityMetaData> systemEntityMetaDataMap = new HashMap<>();

	public SystemEntityMetaData getSystemEntityMetaData(String entityName)
	{
		return systemEntityMetaDataMap.get(entityName);
	}

	public void addSystemEntityMetaData(SystemEntityMetaData systemEntityMetaData)
	{
		String systemEntityMetaDataName = systemEntityMetaData.getName();
		if (systemEntityMetaDataName == null)
		{
			throw new IllegalArgumentException(format("[%s] is missing name, did you forget to call setName()?",
					systemEntityMetaData.getClass().getSimpleName()));
		}

		LOG.trace("Registering system entity [{}] ...", systemEntityMetaDataName);
		systemEntityMetaDataMap.put(systemEntityMetaDataName, systemEntityMetaData);
	}
}
