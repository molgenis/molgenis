package org.molgenis.security.core.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Optional;

import static com.google.common.collect.Range.atLeast;
import static com.google.common.collect.Range.closed;

/**
 * A User's membership of a Group for a certain period of validity.
 */
@AutoValue
public abstract class GroupMembership
{
	public abstract String getId();

	public abstract User getUser();

	public abstract Group getGroup();

	public abstract Instant getStart();

	public abstract Optional<Instant> getEnd();

	public Range<Instant> getValidity()
	{
		return getEnd().map(end -> closed(getStart(), end)).orElse(atLeast(getStart()));
	}

	public boolean isCurrent()
	{
		return getValidity().contains(Instant.now());
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

		public abstract GroupMembership build();
	}

}
