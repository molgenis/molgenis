package org.molgenis.auth;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;

import static org.molgenis.auth.GroupAuthorityMetaData.GROUP;
import static org.molgenis.auth.GroupAuthorityMetaData.ID;

public class GroupAuthority extends Authority
{
	public GroupAuthority(Entity entity)
	{
		super(entity);
	}

	public GroupAuthority(EntityType entityType)
	{
		super(entityType);
	}

	public GroupAuthority(String id, EntityType entityType)
	{
		super(entityType);
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

	public Group getGroup()
	{
		return getEntity(GROUP, Group.class);
	}

	public void setGroup(Group group)
	{
		set(GROUP, group);
	}
}
