package org.molgenis.data.annotation.impl.cmdlineannotatorsettingsconfigurer;

import org.molgenis.data.annotation.CmdLineAnnotatorSettingsConfigurer;

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
