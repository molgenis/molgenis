package org.molgenis.data.security.auth;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import java.time.Instant;
import java.util.Optional;

import static java.time.Instant.now;
import static org.molgenis.data.security.auth.GroupMembershipMetadata.*;

public class GroupMembership extends StaticEntity
{
	public GroupMembership(Entity entity)
	{
		super(entity);
	}

	public GroupMembership(EntityType entityType)
	{
		super(entityType);
	}

	public GroupMembership(String id, EntityType entityType)
	{
		super(entityType);
		setId(id);
	}

	public void setId(String id)
	{
		set(ID, id);
	}

	public String getId()
	{
		return getString(ID);
	}

	public void setUser(User user)
	{
		set(USER, user);
	}

	public User getUser()
	{
		return getEntity(USER, User.class);
	}

	public void setRole(Role role)
	{
		set(ROLE, role);
	}

	public Role getRole()
	{
		return getEntity(ROLE, Role.class);
	}

	public void setFrom(Instant from)
	{
		set(FROM, from);
	}

	public Instant getFrom()
	{
		return getInstant(FROM);
	}

	public void setTo(Instant to)
	{
		set(TO, to);
	}

	public Optional<Instant> getTo()
	{
		return Optional.ofNullable(getInstant(TO));
	}

	public boolean isCurrentlyActive()
	{
		return getFrom().isBefore(now()) && !getTo().filter(now()::isAfter).isPresent();
	}
}
