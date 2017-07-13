package org.molgenis.data.security.acl;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
public abstract class EntityAcl
{
	public abstract EntityIdentity getEntityIdentity();

	public abstract SecurityId getOwner();

	@Nullable
	public abstract EntityAcl getParent();

	public abstract List<EntityAce> getEntries();

	public static EntityAcl create(EntityIdentity newEntityIdentity, SecurityId newOwner, EntityAcl newParent,
			List<EntityAce> newEntries)
	{
		return builder().setEntityIdentity(newEntityIdentity).setOwner(newOwner).setParent(newParent)
				.setEntries(newEntries).build();
	}

	public abstract Builder toBuilder();

	public static Builder builder()
	{
		return new AutoValue_EntityAcl.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder
	{
		public abstract Builder setEntityIdentity(EntityIdentity newEntityIdentity);

		public abstract Builder setOwner(SecurityId newOwner);

		public abstract Builder setParent(EntityAcl newParent);

		public abstract Builder setEntries(List<EntityAce> newEntries);

		public abstract EntityAcl build();
	}


}
