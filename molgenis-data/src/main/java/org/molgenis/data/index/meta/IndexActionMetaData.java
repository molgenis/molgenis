package org.molgenis.data.index.meta;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.index.meta.IndexPackage.PACKAGE_INDEX;

/**
 * The index action is used to describe the action that needs to be done to make a
 * {@link org.molgenis.data.Repository}'s index consistent again.
 */
@Component
public class IndexActionMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "IndexAction";
	public static final String INDEX_ACTION = PACKAGE_INDEX + PACKAGE_SEPARATOR + SIMPLE_NAME;

	/**
	 * Is auto generated
	 */
	public static final String ID = "id";

	/**
	 * The group that this index action belongs to.
	 */
	public static final String INDEX_ACTION_GROUP_ATTR = "indexActionGroup";

	/**
	 * The order in which the action is registered within its IndexActionJob
	 */
	public static final String ACTION_ORDER = "actionOrder";

	/**
	 * The full name of the entity that needs to be indexed.
	 */
	public static final String ENTITY_FULL_NAME = "entityFullName";

	/**
	 * Entity is filled when only one row of the entity "entityFullName" is indexed
	 */
	public static final String ENTITY_ID = "entityId";

	/**
	 * Enum: the status of index action.
	 *
	 * @see IndexStatus
	 */
	public static final String INDEX_STATUS = "indexStatus";

	private final IndexPackage indexPackage;
	private IndexActionGroupMetaData indexActionGroupMetaData;

	@Autowired
	public IndexActionMetaData(IndexPackage indexPackage, IndexActionGroupMetaData indexActionGroupMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_INDEX);
		this.indexPackage = requireNonNull(indexPackage);
		this.indexActionGroupMetaData = requireNonNull(indexActionGroupMetaData);
	}

	@Override
	public void init()
	{
		setLabel("Index action");
		setPackage(indexPackage);

		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false);
		addAttribute(INDEX_ACTION_GROUP_ATTR).setDescription("The group that this index action belongs to")
				.setDataType(XREF).setRefEntity(indexActionGroupMetaData);
		addAttribute(ACTION_ORDER).setDataType(INT)
				.setDescription("The order in which the action is registered within its IndexActionJob")
				.setNillable(false);
		addAttribute(ENTITY_FULL_NAME).setDescription("The full name of the entity that needs to be indexed.")
				.setNillable(false);
		addAttribute(ENTITY_ID)
				.setDescription("Filled when only one row of the entity \"" + ENTITY_FULL_NAME + "\" is indexed")
				.setDataType(TEXT).setNillable(true);
		addAttribute(INDEX_STATUS).setDescription("The status of index action").setDataType(ENUM)
				.setEnumOptions(IndexStatus.class).setNillable(false);
	}

	/**
	 * Index action status
	 */
	public enum IndexStatus
	{
		/**
		 * index action is finished
		 */
		FINISHED,
		/**
		 * index action is canceled
		 */
		CANCELED,
		/**
		 * index action failed
		 */
		FAILED,
		/**
		 * index action is started
		 */
		STARTED,
		/**
		 * index action is just created and is not yet processed
		 */
		PENDING
	}
}