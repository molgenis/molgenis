package org.molgenis.compute.ui.model;

import org.molgenis.compute.ui.meta.AnalysisMetaData;
import org.molgenis.compute.ui.meta.UIBackendMetaData;
import org.molgenis.data.support.MapEntity;

public class UIBackend extends MapEntity
{
	private static final long serialVersionUID = 3804467785305796766L;

	public UIBackend()
	{
		super(UIBackendMetaData.IDENTIFIER);
	}

	public UIBackend(String identifier)
	{
		this();
		setIdentifier(identifier);
	}

	public void setIdentifier(String identifier)
	{
		set(AnalysisMetaData.IDENTIFIER, identifier);
	}

	public String getIdentifier()
	{
		return getString(AnalysisMetaData.IDENTIFIER);
	}

	public void setHost(String host)
	{
		set(UIBackendMetaData.HOST, host);
	}

	public String getHost()
	{
		return getString(UIBackendMetaData.HOST);
	}

	public void setHeaderCallback(String headerCallback)
	{
		set(UIBackendMetaData.HEADER_CALLBACK, headerCallback);
	}

	public String getHeaderCallback()
	{
		return getString(UIBackendMetaData.HEADER_CALLBACK);
	}

	public String getFooterCallback()
	{
		return getString(UIBackendMetaData.FOOTER_CALLBACK);
	}

	public void setFooterCallback(String footerCallback)
	{
		set(UIBackendMetaData.FOOTER_CALLBACK, footerCallback);
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
		result = prime * result + getIdentifier().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		return getIdentifier().equals(((UIBackend) obj).getIdentifier());
	}
}
