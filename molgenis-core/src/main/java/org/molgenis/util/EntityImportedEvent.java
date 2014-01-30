package org.molgenis.util;

import org.springframework.context.ApplicationEvent;

/**
 * Spring application event that is published when a entity was imported
 * 
 * @author erwin
 */
public class EntityImportedEvent extends ApplicationEvent
{
	private static final long serialVersionUID = 1L;
	private final Integer entityId;
	private final String entityName;

	public EntityImportedEvent(Object source, String entityName, Integer entityId)
	{
		super(source);
		if (entityName == null) throw new IllegalArgumentException("entityName is null");
		if (entityId == null) throw new IllegalArgumentException("entityId is null");
		this.entityName = entityName;
		this.entityId = entityId;
	}

	public String getEntityName()
	{
		return entityName;
	}

	public Integer getEntityId()
	{
		return entityId;
	}
}
