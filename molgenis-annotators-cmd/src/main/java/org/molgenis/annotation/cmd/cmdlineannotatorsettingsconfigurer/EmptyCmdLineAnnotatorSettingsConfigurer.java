package org.molgenis.annotation.cmd.cmdlineannotatorsettingsconfigurer;

import org.molgenis.data.annotation.core.resources.CmdLineAnnotatorSettingsConfigurer;

/**
 * CmdLineAnnotatorSettingsConfigurer that adds no properties
 */
public class EmptyCmdLineAnnotatorSettingsConfigurer implements CmdLineAnnotatorSettingsConfigurer
{
	@Override
	public void addSettings(String annotationSourceFileName)
	{
	}
}
