package org.molgenis.security.core.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import org.molgenis.gson.AutoGson;

import java.util.List;
import java.util.Optional;

/**
 * A Group of {@link User}s.
 */
@AutoValue
@AutoGson(autoValueClass = AutoValue_Group.class)
@SuppressWarnings("squid:S1610")
public abstract class Group
{
	public abstract Optional<String> getId();

	public abstract String getLabel();

	public abstract Optional<Group> getParent();

	public abstract ImmutableList<Role> getRoles();

	public boolean hasSameParentAs(Group other)
	{
		return getParent().filter(parent -> other.getParent().filter(parent::equals).isPresent()).isPresent();
	}

	public abstract Group.Builder toBuilder();

	public static Builder builder()
	{
		return new AutoValue_Group.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder
	{
		public abstract Builder id(String id);

		public abstract Builder label(String label);

		public abstract Builder parent(Group parent);

		public abstract Builder roles(List<Role> roles);

		protected abstract ImmutableList.Builder<Role> rolesBuilder();

		public Builder addRole(Role role)
		{
			rolesBuilder().add(role);
			return this;
		}

		public abstract Group build();
	}
}
