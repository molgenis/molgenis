package org.molgenis.data.security.model;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;
import org.molgenis.security.core.model.GroupMembership;

import java.time.Instant;
import java.util.Optional;

import static org.molgenis.data.security.model.GroupMembershipMetadata.*;

public class GroupMembershipEntity extends StaticEntity
{
	public GroupMembershipEntity(Entity entity)
	{
		super(entity);
	}

	public GroupMembershipEntity(EntityType entityType)
	{
		super(entityType);
	}

	public GroupMembershipEntity(String id, EntityType entityType)
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

	public UserEntity getUser()
	{
		return getEntity(USER, UserEntity.class);
	}

	public void setUser(UserEntity user)
	{
		set(USER, user);
	}

	public void setUser(String userId)
	{
		set(USER, userId);
	}

	public GroupEntity getGroup()
	{
		return getEntity(GROUP, GroupEntity.class);
	}

	public void setGroup(GroupEntity group)
	{
		set(GROUP, group);
	}

	public void setGroup(String groupId)
	{
		set(GROUP, groupId);
	}

	public Instant getStart()
	{
		return getInstant(START);
	}

	public void setStart(Instant start)
	{
		set(START, start);
	}

	public Optional<Instant> getEnd()
	{
		return Optional.ofNullable(getInstant(END));
	}

	public void setEnd(Instant end)
	{
		set(END, end);
	}

	public GroupMembership toGroupMembership()
	{
		GroupMembership.Builder result = GroupMembership.builder()
														.id(getId())
														.user(getUser().toUser())
														.group(getGroup().toGroup())
														.start(getStart());
		getEnd().ifPresent(result::end);
		return result.build();
	}

	public GroupMembershipEntity updateFrom(GroupMembership groupMembership)
	{
		setUser(groupMembership.getUser().getId().orElseThrow(() -> new IllegalArgumentException("User has empty id")));
		setGroup(groupMembership.getGroup()
								.getId()
								.orElseThrow(() -> new IllegalArgumentException("Group has empty id")));
		setStart(groupMembership.getStart());
		groupMembership.getId().ifPresent(this::setId);
		groupMembership.getEnd().ifPresent(this::setEnd);
		return this;
	}
}
