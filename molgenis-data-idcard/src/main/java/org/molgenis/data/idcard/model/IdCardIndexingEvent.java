package org.molgenis.data.idcard.model;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntity;

public class IdCardIndexingEvent extends DefaultEntity
{
	private static final long serialVersionUID = 1L;

	public static final String ENTITY_NAME = "IdCardIndexingEvent";
	public static final EntityMetaData META_DATA = new IdCardIndexingEventMetaData();

	public static final String ID = "id";
	public static final String DATE = "date";
	public static final String STATUS = "status";
	public static final String MESSAGE = "message";

	public IdCardIndexingEvent(DataService dataService)
	{
		super(META_DATA, dataService);
	}

	public IdCardIndexingEventStatus getStatus()
	{
		String statusStr = getString(IdCardIndexingEvent.STATUS);
		return statusStr != null ? IdCardIndexingEventStatus.valueOf(statusStr) : null;
	}

	public void setStatus(IdCardIndexingEventStatus idCardIndexingEventStatus)
	{
		set(IdCardIndexingEvent.STATUS, idCardIndexingEventStatus.toString());
	}

	public String getMessage()
	{
		return getString(IdCardIndexingEvent.MESSAGE);
	}

	public void setMessage(String message)
	{
		set(IdCardIndexingEvent.MESSAGE, message);
	}
}
