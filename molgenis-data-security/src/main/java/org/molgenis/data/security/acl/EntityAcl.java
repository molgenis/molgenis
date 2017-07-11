package org.molgenis.data.security.acl;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
public abstract class EntityAcl
{
	public abstract String getEntityTypeId();

	public abstract Object getEntityId();

	public abstract SecurityId getOwner();

	@Nullable
	public abstract EntityAcl getParent();

	public abstract List<EntityAce> getEntries();

	public abstract Builder toBuilder();

	public static EntityAcl create(String newEntityTypeId, Object newEntityId, SecurityId newOwner, EntityAcl newParent,
			List<EntityAce> newEntries)
	{
		return builder().setEntityTypeId(newEntityTypeId).setEntityId(newEntityId).setOwner(newOwner)
				.setParent(newParent).setEntries(newEntries).build();
	}

	public static Builder builder()
	{
		return new AutoValue_EntityAcl.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder
	{
		public abstract Builder setEntityTypeId(String newEntityTypeId);

		public abstract Builder setEntityId(Object newEntityId);

		public abstract Builder setOwner(SecurityId newOwner);

		public abstract Builder setParent(EntityAcl newParent);

		public abstract Builder setEntries(List<EntityAce> newEntries);

		public abstract EntityAcl build();
	}
}
