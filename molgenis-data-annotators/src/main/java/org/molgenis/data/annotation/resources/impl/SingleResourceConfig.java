package org.molgenis.data.annotation.resources.impl;

import java.io.File;

import org.molgenis.data.annotation.resources.ResourceConfig;
import org.molgenis.framework.server.MolgenisSettings;

/**
 * Created by charbonb on 16/06/15.
 */
public class SingleResourceConfig implements ResourceConfig
{

	private File file;
	private MolgenisSettings molgenisSettings;
	private String fileProperty;

	public SingleResourceConfig(String fileProperty, MolgenisSettings molgenisSettings)
	{
		this.molgenisSettings = molgenisSettings;
		this.fileProperty = fileProperty;
	}

	@Override
	public File getFile()
	{
		return new File(molgenisSettings.getProperty(fileProperty));
	}
}
