package org.molgenis.auth;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.auth.GroupMemberMetaData.*;

public class GroupMember extends StaticEntity
{
	public GroupMember(Entity entity)
	{
		super(entity);
	}

	public GroupMember(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public GroupMember(String id, EntityMetaData entityMeta)
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

	public User getUser()
	{
		return getEntity(USER, User.class);
	}

	public void setUser(User user)
	{
		set(USER, user);
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
