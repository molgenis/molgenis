package org.molgenis.data.reindex.meta;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultEntityMetaData;

/**
 * This entity is used to group the reindex actions.
 */
public class ReindexActionJobMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "ReindexActionJob";

	/**
	 * Example: Transaction id can be used to group all actions into one transaction.
	 */
	public static final String ID = "id";

	/**
	 * The amount of actions that are grouped together. It is being used to determine the order of the action.
	 */
	public static final String COUNT = "count";

	public ReindexActionJobMetaData(String backend)
	{
		super(ENTITY_NAME);
		setDescription("This entity is used to group the reindex actions.");
		setBackend(backend);
		addAttribute(ID, ROLE_ID);
		addAttribute(COUNT).setDataType(MolgenisFieldTypes.INT).setNillable(false);
	}
}
