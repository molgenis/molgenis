package org.molgenis.data.system;

import com.google.auto.value.AutoValue;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.system.ImportRunMetaData;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

@AutoValue
public class ImportRun extends org.molgenis.data.support.AbstractEntity implements org.molgenis.data.Entity
{

	public static final String ENTITY_NAME = "ImportRun";

	private static final java.util.List<org.molgenis.util.ValueLabel> status_options;
	private static final java.util.List<org.molgenis.util.ValueLabel> notify_options;

	private String id;
	private Date startDate;
	private Date endDate;
	private String userName;
	private String status;
	private String message;
	private int progress;
	private String importedEntities;
	private boolean notify;
	private String status_label;
	private String notify_label;

	static
	{
		status_options = new java.util.ArrayList<org.molgenis.util.ValueLabel>();
		status_options.add(new org.molgenis.util.ValueLabel("RUNNING", "RUNNING"));
		status_options.add(new org.molgenis.util.ValueLabel("FINISHED", "FINISHED"));
		status_options.add(new org.molgenis.util.ValueLabel("FAILED", "FAILED"));
	}

	static
	{
		notify_options = new java.util.ArrayList<org.molgenis.util.ValueLabel>();
		notify_options.add(new org.molgenis.util.ValueLabel("API", "API"));
		notify_options.add(new org.molgenis.util.ValueLabel("UI", "UI"));
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public Date getStartDate()
	{
		return startDate;
	}

	public void setStartDate(Date startDate)
	{
		this.startDate = startDate;
	}

	public Date getEndDate()
	{
		return endDate;
	}

	public void setEndDate(Date endDate)
	{
		this.endDate = endDate;
	}

	public String getUserName()
	{
		return userName;
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public int getProgress()
	{
		return progress;
	}

	public void setProgress(int progress)
	{
		this.progress = progress;
	}

	public String getImportedEntities()
	{
		return importedEntities;
	}

	public void setImportedEntities(String importedEntities)
	{
		this.importedEntities = importedEntities;
	}

	public String getStatusLabel()
	{
		return this.status_label;
	}

	public boolean getNotify()
	{
		return notify;
	}

	public void setNotify(boolean notify)
	{
		this.notify = notify;
	}

	/**
	 * Status is enum. This method returns all available enum options.
	 */
	public java.util.List<org.molgenis.util.ValueLabel> getStatusOptions()
	{
		return status_options;
	}

	public java.util.List<org.molgenis.util.ValueLabel> getnotifyOptions()
	{
		return notify_options;
	}

	@Override
	public Object get(String name)
	{
		if (name.equals("id")) return getId();
		if (name.equals("startDate")) return getStartDate();
		if (name.equals("endDate")) return getEndDate();
		if (name.equals("userName")) return getUserName();
		if (name.equals("status")) return getStatus();
		if (name.equals("status_label")) return getStatusLabel();
		if (name.equals("message")) return getMessage();
		if (name.equals("progress")) return getProgress();
		if (name.equals("importedEntities")) return getImportedEntities();
		if (name.equals("notify")) return getNotify();
		return null;
	}

	@Override
	public void set(Entity entity)
	{
		set(entity, true);
	}

	public void set(org.molgenis.data.Entity entity, boolean strict)
	{
		if (entity.getString("id") != null) this.setId(entity.getString("id"));
		if (entity.getString("ImportRun_id") != null) this.setId(entity.getString("ImportRun_id"));
		if (entity.getTimestamp("startDate") != null) this.setStartDate(entity.getTimestamp("startDate"));
		if (entity.getTimestamp("ImportRun_startDate") != null)
			this.setStartDate(entity.getTimestamp("ImportRun_startDate"));
		if (entity.getTimestamp("endDate") != null) this.setEndDate(entity.getTimestamp("endDate"));
		if (entity.getTimestamp("ImportRun_endDate") != null) this.setEndDate(entity.getTimestamp("ImportRun_endDate"));
		if (entity.getString("userName") != null) this.setUserName(entity.getString("userName"));
		if (entity.getString("ImportRun_userName") != null) this.setUserName(entity.getString("ImportRun_userName"));
		if (entity.getString("status") != null) this.setStatus(entity.getString("status"));
		if (entity.getString("ImportRun_status") != null) this.setStatus(entity.getString("ImportRun_status"));
		if (entity.getString("message") != null) this.setMessage(entity.getString("message"));
		if (entity.getString("ImportRun_message") != null) this.setMessage(entity.getString("ImportRun_message"));
		if (entity.getInt("progress") != null) this.setProgress(entity.getInt("progress"));
		if (entity.getInt("ImportRun_progress") != null) this.setProgress(entity.getInt("ImportRun_progress"));
		if (entity.getString("importedEntities") != null)
			this.setImportedEntities(entity.getString("importedEntities"));
		if (entity.getString("ImportRun_importedEntities") != null)
			this.setImportedEntities(entity.getString("ImportRun_importedEntities"));
		if (entity.getBoolean("notify") != null) this.setNotify(entity.getBoolean("notify"));
		if (entity.getString("ImportRun_notify") != null) this.setNotify(entity.getBoolean("ImportRun_notify"));

	}

	@Override
	public String toString()
	{
		return this.toString(false);
	}

	public String toString(boolean verbose)
	{
		StringBuilder sb = new StringBuilder("ImportRun(");
		sb.append("id='" + getId() + "' ");
		sb.append("startDate='" + (getStartDate() == null ? ""
				: new java.text.SimpleDateFormat("MMMM d, yyyy, HH:mm:ss", java.util.Locale.US).format(getStartDate()))
				+ "' ");
		sb.append("endDate='" + (getEndDate() == null ? ""
				: new java.text.SimpleDateFormat("MMMM d, yyyy, HH:mm:ss", java.util.Locale.US).format(getEndDate()))
				+ "' ");
		sb.append("userName='" + getUserName() + "' ");
		sb.append("status='" + getStatus() + "' ");
		sb.append("message='" + getMessage() + "' ");
		sb.append("progress='" + getProgress() + "' ");
		sb.append("importedEntities='" + getImportedEntities() + "'");
		sb.append("notify='" + getNotify() + "'");
		sb.append(");");
		return sb.toString();
	}

	@Override
	public String getIdValue()
	{
		return getId();
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		Set<String> attributeNames = new LinkedHashSet<String>();
		for (AttributeMetaData attr : new ImportRunMetaData().getAttributes())
		{
			attributeNames.add(attr.getName());
		}

		return attributeNames;
	}

	@Override
	public void set(String attributeName, Object value)
	{
		if ("id".equalsIgnoreCase(attributeName))
		{
			this.setId((String) value);
			return;
		}
		if ("startDate".equalsIgnoreCase(attributeName))
		{
			this.setStartDate((java.util.Date) value);
			return;
		}
		if ("endDate".equalsIgnoreCase(attributeName))
		{
			this.setEndDate((java.util.Date) value);
			return;
		}
		if ("userName".equalsIgnoreCase(attributeName))
		{
			this.setUserName((String) value);
			return;
		}
		if ("status".equalsIgnoreCase(attributeName))
		{
			this.setStatus((String) value);
			return;
		}
		if ("message".equalsIgnoreCase(attributeName))
		{
			this.setMessage((String) value);
			return;
		}
		if ("progress".equalsIgnoreCase(attributeName))
		{
			this.setProgress((Integer) value);
			return;
		}
		if ("importedEntities".equalsIgnoreCase(attributeName))
		{
			this.setImportedEntities((String) value);
			return;
		}
		if ("notify".equalsIgnoreCase(attributeName))
		{
			this.setNotify((Boolean) value);
			return;
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		int result = 1;
		return result;
	}

	@Override
	public org.molgenis.data.EntityMetaData getEntityMetaData()
	{
		return new ImportRunMetaData();
	}
}
