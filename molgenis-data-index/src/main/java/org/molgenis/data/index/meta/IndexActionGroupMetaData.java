package org.molgenis.data.index.meta;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.index.meta.IndexPackage.PACKAGE_INDEX;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

/**
 * This entity is used to group the index actions.
 */
@Component
public class IndexActionGroupMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "IndexActionGroup";
	public static final String INDEX_ACTION_GROUP = PACKAGE_INDEX + PACKAGE_SEPARATOR + SIMPLE_NAME;

	/**
	 * Example: Transaction id can be used to group all actions into one transaction.
	 */
	public static final String ID = "id";

	/**
	 * The amount of actions that are grouped together. It is being used to determine the order of the action.
	 */
	public static final String COUNT = "count";

	private final IndexPackage indexPackage;

	public IndexActionGroupMetaData(IndexPackage indexPackage)
	{
		super(SIMPLE_NAME, PACKAGE_INDEX);
		this.indexPackage = requireNonNull(indexPackage);
	}

	@Override
	public void init()
	{
		setLabel("Index action group");
		setPackage(indexPackage);

		setDescription("This entity is used to group the index actions.");
		addAttribute(ID, ROLE_ID);
		addAttribute(COUNT).setDataType(INT).setNillable(false);
	}
}
