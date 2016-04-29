package org.molgenis.data.elasticsearch.reindex;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultEntityMetaData;

public class ReindexActionJobMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "reindex_action_job";
	public static final String ID = "id";
	public static final String COUNT = "count";

	public ReindexActionJobMetaData(String backend)
	{
		super(ENTITY_NAME);
		setBackend(backend);
		addAttribute(ID, ROLE_ID);
		addAttribute(COUNT).setDataType(MolgenisFieldTypes.INT).setNillable(false);
	}
}
