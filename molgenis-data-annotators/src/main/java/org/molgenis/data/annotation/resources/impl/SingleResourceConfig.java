package org.molgenis.data.annotation.resources.impl;

import java.io.File;

import org.molgenis.data.annotation.resources.ResourceConfig;
import org.molgenis.data.annotation.settings.AnnotationSettings;

/**
 * Created by charbonb on 16/06/15.
 */
public class SingleResourceConfig implements ResourceConfig
{
	private AnnotationSettings annotationSettings;
	private String fileProperty;

	public SingleResourceConfig(String fileProperty, AnnotationSettings annotationSettings)
	{
		this.annotationSettings = annotationSettings;
		this.fileProperty = fileProperty;
	}

	@Override
	public File getFile()
	{
		String file = annotationSettings.getString(fileProperty);
		if (null != file && !file.isEmpty())
		{
			return new File(annotationSettings.getString(fileProperty));
		}
		return null;
	}
}
