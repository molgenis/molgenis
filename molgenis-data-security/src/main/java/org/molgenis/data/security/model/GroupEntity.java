package org.molgenis.data.security.model;

import com.google.common.collect.Lists;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;
import org.molgenis.security.core.model.Group;

import java.util.Optional;

import static com.google.common.collect.Streams.stream;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.security.model.GroupMetadata.*;

public class GroupEntity extends StaticEntity
{
	public GroupEntity(Entity entity)
	{
		super(entity);
	}

	public GroupEntity(EntityType entityType)
	{
		super(entityType);
	}

	public GroupEntity(String id, EntityType entityType)
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

	public String getLabel()
	{
		return getString(LABEL);
	}

	public void setLabel(String name)
	{
		set(LABEL, name);
	}

	public Optional<GroupEntity> getParent()
	{
		return Optional.ofNullable(getEntity(PARENT, GroupEntity.class));
	}

	public void setParent(GroupEntity parent)
	{
		set(PARENT, parent);
	}

	public Iterable<GroupEntity> getChildren()
	{
		return Lists.newArrayList(getEntities(CHILDREN, GroupEntity.class));
	}

	public Iterable<RoleEntity> getRoles()
	{
		return getEntities(ROLES, RoleEntity.class);
	}

	public Group toGroup()
	{
		Group.Builder result = Group.builder()
									.id(getId())
									.label(getLabel())
									.roles(stream(getRoles()).map(RoleEntity::toRole).collect(toList()));
		getParent().ifPresent(parent -> result.parent(parent.toGroup()));
		return result.build();
	}
}
