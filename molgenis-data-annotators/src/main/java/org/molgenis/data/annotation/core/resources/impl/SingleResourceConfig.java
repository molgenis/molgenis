package org.molgenis.data.annotation.core.resources.impl;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.resources.ResourceConfig;
import org.molgenis.security.core.runas.RunAsSystemProxy;

import java.io.File;

public class SingleResourceConfig implements ResourceConfig
{
	private final Entity pluginSettings;
	private final String fileAttribute;

	public SingleResourceConfig(String fileAttribute, Entity pluginSettings)
	{
		this.pluginSettings = pluginSettings;
		this.fileAttribute = fileAttribute;
	}

	@Override
	public File getFile()
	{
		String file = RunAsSystemProxy.runAsSystem(() -> pluginSettings.getString(fileAttribute));
		if (null != file && !file.isEmpty())
		{
			return new File(file);
		}
		return null;
	}
}
