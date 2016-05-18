package org.molgenis.data.reindex.meta;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

/**
 * This entity is used to group the reindex actions.
 */
@Component
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

	public ReindexActionJobMetaData()
	{
		super(ENTITY_NAME);
		setDescription("This entity is used to group the reindex actions.");
		addAttribute(ID, ROLE_ID);
		addAttribute(COUNT).setDataType(MolgenisFieldTypes.INT).setNillable(false);
	}
}
