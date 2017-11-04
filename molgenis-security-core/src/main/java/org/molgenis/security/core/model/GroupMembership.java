package org.molgenis.security.core.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Optional;

import static com.google.common.collect.Range.atLeast;
import static com.google.common.collect.Range.closedOpen;

/**
 * A User's membership of a Group for a certain period of validity.
 */
@AutoValue
@SuppressWarnings("squid:S1610")
public abstract class GroupMembership
{
	public abstract Optional<String> getId();

	public abstract User getUser();

	public abstract Group getGroup();

	public abstract Instant getStart();

	public abstract Optional<Instant> getEnd();

	public Range<Instant> getValidity()
	{
		return getEnd().map(end -> closedOpen(getStart(), end)).orElse(atLeast(getStart()));
	}

	public boolean isOverlappingWith(GroupMembership other)
	{
		return getGroup().hasSameParentAs(other.getGroup()) && getValidity().isConnected(other.getValidity());
	}

	public boolean isSameGroup(GroupMembership other)
	{
		return other.getGroup().equals(getGroup());
	}

	public boolean isCurrent()
	{
		return getValidity().contains(Instant.now());
	}

	public Builder toBuilder()
	{
		Builder result = builder().user(getUser()).group(getGroup()).validity(getValidity());
		getId().ifPresent(result::id);
		return result;
	}

	public static Builder builder()
	{
		return new AutoValue_GroupMembership.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder
	{
		public abstract Builder id(String id);

		public abstract Builder user(User user);

		public abstract Builder group(Group group);

		public abstract Builder start(Instant start);

		public abstract Builder end(Instant end);

		public Builder validity(Range<Instant> validity)
		{
			start(validity.lowerEndpoint());
			if (validity.hasUpperBound())
			{
				end(validity.upperEndpoint());
			}
			return this;
		}

		public abstract GroupMembership build();
	}

}
