package org.molgenis.data.idcard.model;

import static org.molgenis.data.idcard.model.IdCardIndexingEventMetaData.ID;
import static org.molgenis.data.idcard.model.IdCardIndexingEventMetaData.MESSAGE;
import static org.molgenis.data.idcard.model.IdCardIndexingEventMetaData.STATUS;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

public class IdCardIndexingEvent extends StaticEntity
{
	public IdCardIndexingEvent(Entity entity)
	{
		super(entity);
	}

	public IdCardIndexingEvent(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public IdCardIndexingEvent(String id, EntityMetaData entityMeta)
	{
		super(entityMeta);
		set(ID, id);
	}

	public IdCardIndexingEventStatus getStatus()
	{
		String statusStr = getString(STATUS);
		return statusStr != null ? IdCardIndexingEventStatus.valueOf(statusStr) : null;
	}

	public void setStatus(IdCardIndexingEventStatus idCardIndexingEventStatus)
	{
		set(STATUS, idCardIndexingEventStatus.toString());
	}

	public String getMessage()
	{
		return getString(MESSAGE);
	}

	public void setMessage(String message)
	{
		set(MESSAGE, message);
	}
}
