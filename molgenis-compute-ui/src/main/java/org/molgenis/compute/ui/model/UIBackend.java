package org.molgenis.compute.ui.model;

import org.molgenis.compute.ui.meta.UIBackendMetaData;
import org.molgenis.data.support.MapEntity;

public class UIBackend extends MapEntity
{

	public UIBackend()
	{
		super(UIBackendMetaData.URL);
	}

	public UIBackend(String url)
	{
		this();
		setUrl(url);
	}

	public void setUrl(String url)
	{
		set(UIBackendMetaData.URL, url);
	}

	public String getUrl()
	{
		return getString(UIBackendMetaData.URL);
	}

	public String getWorkDir()
	{
		return getString(UIBackendMetaData.WORK_DIR);
	}

	public void setWorkDir(String workDir)
	{
		set(UIBackendMetaData.WORK_DIR, workDir);
	}
}
