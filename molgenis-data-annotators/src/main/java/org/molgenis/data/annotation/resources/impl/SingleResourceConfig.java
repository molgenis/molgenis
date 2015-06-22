package org.molgenis.data.annotation.resources.impl;

import org.molgenis.data.annotation.resources.ResourceConfig;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.security.core.MolgenisPermissionService;

import java.io.File;

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
		if(file == null) file = new File(molgenisSettings.getProperty(fileProperty));
		return file;
	}
}
