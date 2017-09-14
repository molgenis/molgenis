package org.molgenis.data.index.meta;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.index.meta.IndexPackage.PACKAGE_INDEX;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

/**
 * The index action is used to describe the action that needs to be done to make a
 * {@link org.molgenis.data.Repository}'s index consistent again.
 */
@Component
public class IndexActionMetaData extends SystemEntityType
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
	 * The name of the entity type ID that needs to be indexed
	 */
	public static final String ENTITY_TYPE_ID = "entityTypeId";

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
											 .setDataType(XREF)
											 .setRefEntity(indexActionGroupMetaData);
		addAttribute(ACTION_ORDER).setDataType(INT)
								  .setDescription(
										  "The order in which the action is registered within its IndexActionJob")
								  .setNillable(false);
		addAttribute(ENTITY_TYPE_ID).setDescription(
				"The id of the entity type that needs to be indexed (e.g. myEntityType).").setNillable(false);
		addAttribute(ENTITY_ID).setDescription("The id of the entity that needs to be indexed")
							   .setDataType(TEXT)
							   .setNillable(true);
		addAttribute(INDEX_STATUS).setDescription("The status of index action")
								  .setDataType(ENUM)
								  .setEnumOptions(IndexStatus.class)
								  .setNillable(false);
	}

	/**
	 * Index action status
	 */
	public enum IndexStatus
	{
		/**
		 * index action is finished
		 */
		FINISHED, /**
	 * index action is canceled
	 */
	CANCELED, /**
	 * index action failed
	 */
	FAILED, /**
	 * index action is started
	 */
	STARTED, /**
	 * index action is just created and is not yet processed
	 */
	PENDING
	}
}