package org.molgenis.compute.ui.model;

import org.molgenis.compute.ui.meta.UIBackendMetaData;
import org.molgenis.data.support.MapEntity;

public class UIBackend extends MapEntity
{
	private static final long serialVersionUID = 3804467785305796766L;

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

	public BackendType getType()
	{
		String type = getString(UIBackendMetaData.BACKEND_TYPE);
		if (type == null) return null;
		return BackendType.valueOf(type);
	}

	public void setType(BackendType type)
	{
		String typeStr = type == null ? null : type.toString();
		set(UIBackendMetaData.BACKEND_TYPE, typeStr);
	}

	public SchedulerType getSchedulerType()
	{
		String type = getString(UIBackendMetaData.SCHEDULER_TYPE);
		if (type == null) return null;
		return SchedulerType.valueOf(type);
	}

	public void setSchedulerType(SchedulerType schedulerType)
	{
		String typeStr = schedulerType == null ? null : schedulerType.toString();
		set(UIBackendMetaData.SCHEDULER_TYPE, typeStr);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + getUrl().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		return getUrl().equals(((UIBackend) obj).getUrl());
	}
}
