package org.molgenis.data.index;

import com.google.auto.value.AutoValue;
import org.molgenis.data.EntityKey;

import javax.annotation.Nullable;

/**
 * Value object to store the impact of changes.
 */
@AutoValue
public abstract class Impact
{
	public abstract String getEntityTypeId();

	@Nullable
	public abstract Object getId();

	public boolean isWholeRepository()
	{
		return getId() == null;
	}

	public EntityKey toEntityKey()
	{
		return EntityKey.create(getEntityTypeId(), getId());
	}

	public boolean isSingleEntity()
	{
		return getId() != null;
	}

	public static Impact createSingleEntityImpact(String entityTypeId, Object id)
	{
		return new AutoValue_Impact(entityTypeId, id);
	}

	public static Impact createWholeRepositoryImpact(String entityTypeId)
	{
		return new AutoValue_Impact(entityTypeId, null);
	}
}
