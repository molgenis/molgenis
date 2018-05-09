package org.molgenis.data.security.auth;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.data.security.auth.GroupMemberMetaData.*;

public class GroupMember extends StaticEntity
{
	public GroupMember(Entity entity)
	{
		super(entity);
	}

	public GroupMember(EntityType entityType)
	{
		super(entityType);
	}

	public GroupMember(String id, EntityType entityType)
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

	public User getUser()
	{
		return getEntity(USER, User.class);
	}

	public void setUser(User user)
	{
		set(USER, user);
	}

	public Role getGroup()
	{
		return getEntity(GROUP, Role.class);
	}

	public void setGroup(Role role)
	{
		set(GROUP, role);
	}
}
