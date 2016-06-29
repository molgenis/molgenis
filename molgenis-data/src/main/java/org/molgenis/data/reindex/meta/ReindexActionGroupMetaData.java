package org.molgenis.data.reindex.meta;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.INT;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.reindex.meta.IndexPackage.PACKAGE_INDEX;

/**
 * This entity is used to group the reindex actions.
 */
@Component
public class ReindexActionGroupMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "ReindexActionGroup";
	public static final String REINDEX_ACTION_GROUP = PACKAGE_INDEX + PACKAGE_SEPARATOR + SIMPLE_NAME;

	/**
	 * Example: Transaction id can be used to group all actions into one transaction.
	 */
	public static final String ID = "id";

	/**
	 * The amount of actions that are grouped together. It is being used to determine the order of the action.
	 */
	public static final String COUNT = "count";

	private final IndexPackage indexPackage;

	@Autowired
	public ReindexActionGroupMetaData(IndexPackage indexPackage)
	{
		super(SIMPLE_NAME, PACKAGE_INDEX);
		this.indexPackage = requireNonNull(indexPackage);
	}

	@Override
	public void init()
	{
		setPackage(indexPackage);

		setDescription("This entity is used to group the reindex actions.");
		addAttribute(ID, ROLE_ID);
		addAttribute(COUNT).setDataType(INT).setNillable(false);
	}
}
