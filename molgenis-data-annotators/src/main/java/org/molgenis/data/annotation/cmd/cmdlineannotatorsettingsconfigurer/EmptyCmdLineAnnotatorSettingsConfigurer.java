package org.molgenis.data.annotation.cmd.cmdlineannotatorsettingsconfigurer;

import org.molgenis.data.annotation.cmd.CmdLineAnnotatorSettingsConfigurer;

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
