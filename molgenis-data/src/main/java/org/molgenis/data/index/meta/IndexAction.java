package org.molgenis.data.index.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.data.index.meta.IndexActionMetaData.*;

public class IndexAction extends StaticEntity
{
	public IndexAction(Entity entity)
	{
		super(entity);
	}

	public IndexAction(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public IndexAction(String id, EntityMetaData entityMeta)
	{
		super(entityMeta);
		set(ID, id);
	}

	public String getId()
	{
		return getString(ID);
	}

	public IndexAction setId(String id)
	{
		set(ID, id);
		return this;
	}

	public IndexActionGroup getIndexActionGroup()
	{
		return getEntity(INDEX_ACTION_GROUP_ATTR, IndexActionGroup.class);
	}

	public IndexAction setIndexActionGroup(IndexActionGroup indexActionGroup)
	{
		set(INDEX_ACTION_GROUP_ATTR, indexActionGroup);
		return this;
	}

	public int getActionOrder()
	{
		return getInt(ACTION_ORDER);
	}

	public IndexAction setActionOrder(int actionOrder)
	{
		set(ACTION_ORDER, actionOrder);
		return this;
	}

	public String getEntityFullName()
	{
		return getString(ENTITY_FULL_NAME);
	}

	public IndexAction setEntityFullName(String entityFullName)
	{
		set(ENTITY_FULL_NAME, entityFullName);
		return this;
	}

	public String getEntityId()
	{
		return getString(ENTITY_ID);
	}

	public IndexAction setEntityId(String entityId)
	{
		set(ENTITY_ID, entityId);
		return this;
	}

	public IndexActionMetaData.IndexStatus getIndexStatus()
	{
		String indexStatusStr = getString(INDEX_STATUS);
		return indexStatusStr != null ? IndexStatus.valueOf(indexStatusStr) : null;
	}

	public IndexAction setIndexStatus(IndexActionMetaData.IndexStatus indexStatus)
	{
		set(INDEX_STATUS, indexStatus.toString());
		return this;
	}

	/**
	 * Returns whether two index actions are equal ignoring the auto id
	 *
	 * @param o other
	 * @return {@code true} if this object is the same as the o argument; {@code false} otherwise.
	 */
	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		IndexAction that = (IndexAction) o;

		if (getEntityId() != null ? !getEntityId().equals(that.getEntityId()) : that.getEntityId() != null)
			return false;
		return getEntityFullName() != null ? getEntityFullName().equals(that.getEntityFullName()) :
				that.getEntityFullName() == null;

	}

	/**
	 * Returns a hash code value for this index action ignoring the auto id
	 *
	 * @return a hash code value for this object.
	 */
	@Override
	public int hashCode()
	{
		int result = getEntityId() != null ? getEntityId().hashCode() : 0;
		result = 31 * result + (getEntityFullName() != null ? getEntityFullName().hashCode() : 0);
		return result;
	}
}
