package org.molgenis.data.annotation.resources.impl;

import java.io.File;

import org.molgenis.data.annotation.resources.ResourceConfig;
import org.molgenis.framework.server.MolgenisSettings;

/**
 * Created by charbonb on 16/06/15.
 */
public class SingleResourceConfig implements ResourceConfig
{
	private MolgenisSettings molgenisSettings;
	private String fileProperty;

	public SingleResourceConfig(String fileProperty, MolgenisSettings molgenisSettings)
	{
		System.out.println("SingleResourceConfig --- line 18");
		this.molgenisSettings = molgenisSettings;
		this.fileProperty = fileProperty;
		System.out.println("SingleResourceConfig --- line 21");
	}

	@Override
	public File getFile()
	{
		String file = molgenisSettings.getProperty(fileProperty);
		if (null != file && !file.isEmpty())
		{
			return new File(molgenisSettings.getProperty(fileProperty));
		}
		return null;
	}
}
