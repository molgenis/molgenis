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

	public GroupEntity getGroup()
	{
		return getEntity(GROUP, GroupEntity.class);
	}

	public void setGroup(GroupEntity group)
	{
		set(GROUP, group);
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

	public GroupMembershipEntity updateFrom(GroupMembership groupMembership, UserFactory userFactory,
			GroupFactory groupFactory)
	{
		groupMembership.getUser().getId().map(userFactory::create).ifPresent(this::setUser);
		groupMembership.getGroup().getId().map(groupFactory::create).ifPresent(this::setGroup);
		setStart(groupMembership.getStart());
		groupMembership.getId().ifPresent(this::setId);
		groupMembership.getEnd().ifPresent(this::setEnd);
		return this;
	}
}
