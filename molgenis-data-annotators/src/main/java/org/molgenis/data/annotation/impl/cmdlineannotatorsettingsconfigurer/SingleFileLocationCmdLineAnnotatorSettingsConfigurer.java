package org.molgenis.data.annotation.impl.cmdlineannotatorsettingsconfigurer;

import org.molgenis.data.annotation.CmdLineAnnotatorSettingsConfigurer;
import org.molgenis.framework.server.MolgenisSettings;

public class SingleFileLocationCmdLineAnnotatorSettingsConfigurer implements CmdLineAnnotatorSettingsConfigurer
{
	private final String fileLocationPropertyName;
	private final MolgenisSettings molgenisSettings;

	public SingleFileLocationCmdLineAnnotatorSettingsConfigurer(String fileLocationPropertyName,
			MolgenisSettings molgenisSettings)
	{
		this.fileLocationPropertyName = fileLocationPropertyName;
		this.molgenisSettings = molgenisSettings;
	}

	@Override
	public void addSettings(String annotationSourceFileName)
	{
		molgenisSettings.setProperty(fileLocationPropertyName, annotationSourceFileName);
	}

}
