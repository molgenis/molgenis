package org.molgenis.data.i18n.model;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.data.i18n.model.I18nStringMetaData.*;

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
	 * @param id auto ID
	 * @param entityType internationalized string meta data
	 */
	public I18nString(String id, EntityType entityType)
	{
		super(entityType);
		setId(id);
	}

	public String getId()
	{
		return getString(ID);
	}

	public I18nString setId(String id)
	{
		set(ID, id);
		return this;
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

	public String getNamespace()
	{
		return getString(NAMESPACE);
	}

	public I18nString setNamespace(String namespace)
	{
		set(NAMESPACE, namespace);
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
