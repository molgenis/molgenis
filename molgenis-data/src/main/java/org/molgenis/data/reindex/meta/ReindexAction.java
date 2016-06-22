package org.molgenis.data.reindex.meta;

import static org.molgenis.data.reindex.meta.ReindexActionMetaData.ACTION_ORDER;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.CUD_TYPE;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.DATA_TYPE;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.DataType;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.ENTITY_FULL_NAME;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.ENTITY_ID;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.ID;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.REINDEX_ACTION_GROUP;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.REINDEX_STATUS;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.ReindexStatus;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

public class ReindexAction extends StaticEntity
{
	public ReindexAction(Entity entity)
	{
		super(entity);
	}

	public ReindexAction(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public ReindexAction(String id, EntityMetaData entityMeta)
	{
		super(entityMeta);
		set(ID, id);
	}

	public String getId()
	{
		return getString(ID);
	}

	public ReindexAction setId(String id)
	{
		set(ID, id);
		return this;
	}

	public ReindexActionJob getReindexActionGroup()
	{
		return getEntity(REINDEX_ACTION_GROUP, ReindexActionJob.class);
	}

	public ReindexAction setReindexActionGroup(ReindexActionJob reindexActionGroup)
	{
		set(REINDEX_ACTION_GROUP, reindexActionGroup);
		return this;
	}

	public String getActionOrder()
	{
		return getString(ACTION_ORDER);
	}

	public ReindexAction setActionOrder(String actionOrder)
	{
		set(ACTION_ORDER, actionOrder);
		return this;
	}

	public String getEntityFullName()
	{
		return getString(ENTITY_FULL_NAME);
	}

	public ReindexAction setEntityFullName(String entityFullName)
	{
		set(ENTITY_FULL_NAME, entityFullName);
		return this;
	}

	public String getEntityId()
	{
		return getString(ENTITY_ID);
	}

	public ReindexAction setEntityId(String entityId)
	{
		set(ENTITY_ID, entityId);
		return this;
	}

	public ReindexActionMetaData.CudType getCudType()
	{
		String cudTypeStr = getString(CUD_TYPE);
		return cudTypeStr != null ? CudType.valueOf(cudTypeStr) : null;
	}

	public ReindexAction setCudType(ReindexActionMetaData.CudType cudType)
	{
		set(CUD_TYPE, cudType.toString());
		return this;
	}

	public ReindexActionMetaData.DataType getDataType()
	{
		String dataTypeStr = getString(DATA_TYPE);
		return dataTypeStr != null ? DataType.valueOf(dataTypeStr) : null;
	}

	public ReindexAction setDataType(ReindexActionMetaData.DataType dataType)
	{
		set(DATA_TYPE, dataType.toString());
		return this;
	}

	public ReindexActionMetaData.ReindexStatus getReindexStatus()
	{
		String reindexStatusStr = getString(REINDEX_STATUS);
		return reindexStatusStr != null ? ReindexStatus.valueOf(reindexStatusStr) : null;
	}

	public ReindexAction setReindexStatus(ReindexActionMetaData.ReindexStatus reindexStatus)
	{
		set(REINDEX_STATUS, reindexStatus.toString());
		return this;
	}
}
