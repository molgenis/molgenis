package org.molgenis.data.system;

import static org.molgenis.data.meta.system.ImportRunMetaData.ENDDATE;
import static org.molgenis.data.meta.system.ImportRunMetaData.ID;
import static org.molgenis.data.meta.system.ImportRunMetaData.IMPORTEDENTITIES;
import static org.molgenis.data.meta.system.ImportRunMetaData.MESSAGE;
import static org.molgenis.data.meta.system.ImportRunMetaData.NOTIFY;
import static org.molgenis.data.meta.system.ImportRunMetaData.PROGRESS;
import static org.molgenis.data.meta.system.ImportRunMetaData.STARTDATE;
import static org.molgenis.data.meta.system.ImportRunMetaData.STATUS;
import static org.molgenis.data.meta.system.ImportRunMetaData.USERNAME;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;
import org.molgenis.util.ValueLabel;

public class ImportRun extends StaticEntity
{
	private static final List<ValueLabel> status_options;
	private static final List<ValueLabel> notify_options;

	static
	{
		status_options = new ArrayList<>();
		status_options.add(new org.molgenis.util.ValueLabel("RUNNING", "RUNNING"));
		status_options.add(new org.molgenis.util.ValueLabel("FINISHED", "FINISHED"));
		status_options.add(new org.molgenis.util.ValueLabel("FAILED", "FAILED"));

		notify_options = new ArrayList<>();
		notify_options.add(new org.molgenis.util.ValueLabel("API", "API"));
		notify_options.add(new org.molgenis.util.ValueLabel("UI", "UI"));
	}

	public ImportRun(Entity entity)
	{
		super(entity);
	}

	public ImportRun(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public ImportRun(String id, EntityMetaData entityMeta)
	{
		super(entityMeta);
		setId(id);
	}

	public String getId()
	{
		return getString(ID);
	}

	public void setId(String id)
	{
		set(ID, id);
	}

	public Date getStartDate()
	{
		return getUtilDate(STARTDATE);
	}

	public void setStartDate(Date startDate)
	{
		set(STARTDATE, startDate);
	}

	public Date getEndDate()
	{
		return getUtilDate(ENDDATE);
	}

	public void setEndDate(Date endDate)
	{
		set(ENDDATE, endDate);
	}

	public String getUserName()
	{
		return getString(USERNAME);
	}

	public void setUserName(String userName)
	{
		set(USERNAME, userName);
	}

	public String getStatus()
	{
		return getString(STATUS);
	}

	public void setStatus(String status)
	{
		set(STATUS, status);
	}

	public String getMessage()
	{
		return getString(STATUS);
	}

	public void setMessage(String message)
	{
		set(MESSAGE, message);
	}

	public int getProgress()
	{
		Integer progress = getInt(PROGRESS);
		return progress != null ? progress : 0;
	}

	public void setProgress(int progress)
	{
		set(PROGRESS, progress);
	}

	public String getImportedEntities()
	{
		return getString(IMPORTEDENTITIES);
	}

	public void setImportedEntities(String importedEntities)
	{
		set(IMPORTEDENTITIES, importedEntities);
	}

	public boolean getNotify()
	{
		Boolean notify = getBoolean(NOTIFY);
		return notify != null ? notify : null;
	}

	public void setNotify(boolean notify)
	{
		set(NOTIFY, notify);
	}

	/**
	 * Status is enum. This method returns all available enum options.
	 */
	public List<ValueLabel> getStatusOptions()
	{
		return status_options;
	}

	public List<ValueLabel> getNotifyOptions()
	{
		return notify_options;
	}
}
