package org.molgenis.ui.model;

import org.molgenis.data.Entity;

public class SubjectEntity
{
	private String subject;
	private Entity entity;

	public SubjectEntity(String subject, Entity entity)
	{
		this.subject = subject;
		this.entity = entity;
	}

	public String getSubject()
	{
		return subject;
	}

	public void setSubject(String subject)
	{
		this.subject = subject;
	}

	public Entity getEntity()
	{
		return entity;
	}

	public void setEntity(Entity entity)
	{
		this.entity = entity;
	}
}
