package org.molgenis.data.annotation.impl.cmdlineannotatorsettingsconfigurer;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.CmdLineAnnotatorSettingsConfigurer;

/**
 * CmdLineAnnotatorSettingsConfigurer that defines a single property
 */
public class SingleFileLocationCmdLineAnnotatorSettingsConfigurer implements CmdLineAnnotatorSettingsConfigurer
{
	private final String fileLocationAttributeName;
	private final Entity pluginSettings;

	public SingleFileLocationCmdLineAnnotatorSettingsConfigurer(String fileLocationAttributeName, Entity pluginSettings)
	{
		this.fileLocationAttributeName = fileLocationAttributeName;
		this.pluginSettings = pluginSettings;
	}

	@Override
	public void addSettings(String annotationSourceFileName)
	{
		pluginSettings.set(fileLocationAttributeName, annotationSourceFileName);
	}

}
