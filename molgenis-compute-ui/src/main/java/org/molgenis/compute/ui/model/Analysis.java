package org.molgenis.compute.ui.model;

import java.util.Date;

import org.molgenis.compute.ui.meta.AnalysisMetaData;
import org.molgenis.data.support.MapEntity;

public class Analysis extends MapEntity
{
	private static final long serialVersionUID = 406768955023996782L;

	public Analysis()
	{
		super(AnalysisMetaData.IDENTIFIER);
		// TODO workaround for #1810 'EMX misses DefaultValue'
		setStatus(AnalysisMetaData.STATUS_DEFAULT);
	}

	public Analysis(String identifier, String name)
	{
		this();
		set(AnalysisMetaData.IDENTIFIER, identifier);
		setName(name);
	}

	public String getIdentifier()
	{
		return getString(AnalysisMetaData.IDENTIFIER);
	}

	public String getName()
	{
		return getString(AnalysisMetaData.NAME);
	}

	public void setName(String name)
	{
		set(AnalysisMetaData.NAME, name);
	}

	public String getDescription()
	{
		return getString(AnalysisMetaData.DESCRIPTION);
	}

	public void setDescription(String description)
	{
		set(AnalysisMetaData.DESCRIPTION, description);
	}

	public Date getCreationDate()
	{
		return getUtilDate(AnalysisMetaData.CREATION_DATE);
	}

	public void setCreationDate(Date creationDate)
	{
		set(AnalysisMetaData.CREATION_DATE, creationDate);
	}

	public UIWorkflow getWorkflow()
	{
		return getEntity(AnalysisMetaData.WORKFLOW, UIWorkflow.class);
	}

	public void setWorkflow(UIWorkflow workflow)
	{
		set(AnalysisMetaData.WORKFLOW, workflow);
	}

	public UIBackend getBackend()
	{
		return getEntity(AnalysisMetaData.BACKEND, UIBackend.class);
	}

	public void setBackend(UIBackend backend)
	{
		set(AnalysisMetaData.BACKEND, backend);
	}

	public String getSubmitScript()
	{
		return getString(AnalysisMetaData.SUBMIT_SCRIPT);
	}

	public void setSubmitScript(String submitScript)
	{
		set(AnalysisMetaData.SUBMIT_SCRIPT, submitScript);
	}

	public void setWasRun(boolean wasRun)
	{
		set(AnalysisMetaData.WAS_RUN, wasRun);
	}

	public boolean isWasRun()
	{
		return getBoolean(AnalysisMetaData.WAS_RUN);
	}


	public AnalysisStatus getStatus()
	{
		String status = getString(AnalysisMetaData.STATUS);
		if (status == null)
		{
			// TODO workaround for #1810 'EMX misses DefaultValue'
			return AnalysisMetaData.STATUS_DEFAULT;
		}

		return AnalysisStatus.valueOf(status);
	}

	public void setStatus(AnalysisStatus status)
	{
		set(AnalysisMetaData.STATUS, status.toString());
	}

	public String getUser()
	{
		return getString(AnalysisMetaData.USER);
	}

	public void setUser(String username)
	{
		set(AnalysisMetaData.USER, username);
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

		return getIdentifier().equals(((Analysis) obj).getIdentifier());
	}
}
