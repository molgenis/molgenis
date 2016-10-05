package org.molgenis.data.i18n.model;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.data.i18n.model.I18nStringMetaData.DESCRIPTION;
import static org.molgenis.data.i18n.model.I18nStringMetaData.MSGID;

/**
 * Internationalization string entity
 */
public class I18nString extends StaticEntity
{
	public I18nString(Entity entity)
	{
		super(entity);
	}

	/**
	 * Constructs an internationalized string with the given meta data
	 *
	 * @param entityType language meta data
	 */
	public I18nString(EntityType entityType)
	{
		super(entityType);
	}

	/**
	 * Constructs an internationalized string with the given id and meta data
	 *
	 * @param msgId      message id
	 * @param entityType internationalized string meta data
	 */
	public I18nString(String msgId, EntityType entityType)
	{
		super(entityType);
		setMessageId(msgId);
	}

	public String getMessageId()
	{
		return getString(MSGID);
	}

	public I18nString setMessageId(String msgId)
	{
		set(MSGID, msgId);
		return this;
	}

	public String getDescription()
	{
		return getString(DESCRIPTION);
	}

	public I18nString setDescription(String description)
	{
		set(DESCRIPTION, description);
		return this;
	}
}
