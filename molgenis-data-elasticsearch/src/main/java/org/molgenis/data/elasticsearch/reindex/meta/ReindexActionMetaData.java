package org.molgenis.data.elasticsearch.reindex.meta;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.fieldtypes.EnumField;
import org.molgenis.fieldtypes.TextField;

/**
 * The reindex action is used to describe the action that needs to be done to make a {@link org.molgenis.data.Repository}'s
 * index consistent again.
 */
public class ReindexActionMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "ReindexAction";

	/**
	 * Is auto generated
	 */
	public static final String ID = "id";

	/**
	 * The group that this reindex action belongs to.
	 */
	public static final String REINDEX_ACTION_GROUP = "reindexActionGroup";

	/**
	 * The order in which the action is registered within its ReindexActionJob
	 */
	public static final String ACTION_ORDER = "actionOrder";

	/**
	 * The full name of the entity that needs to be reindexed.
	 */
	public static final String ENTITY_FULL_NAME = "entityFullName";

	/**
	 * Entity is filled when only one row of the entity "entityFullName" is reindexed
	 */
	public static final String ENTITY_ID = "entityId";

	/**
	 * Enum: The create, update and delete operations.
	 *
	 * @see CudType
	 */
	public static final String CUD_TYPE = "cudType";

	/**
	 * Enum: Tells you if the data or the metadata of the "entity_full_name" is affected.
	 *
	 * @see DataType
	 */
	public static final String DATA_TYPE = "dataType";

	/**
	 * Enum: the status of reindex action.
	 *
	 * @see ReindexStatus
	 */
	public static final String REINDEX_STATUS = "reindexStatus";

	public ReindexActionMetaData(ReindexActionJobMetaData indexTransactionLogMetaData, String backend)
	{
		super(ENTITY_NAME);
		setBackend(backend);
		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false);
		addAttribute(REINDEX_ACTION_GROUP).setDescription("The group that this reindex action belongs to")
				.setDataType(MolgenisFieldTypes.XREF).setRefEntity(indexTransactionLogMetaData);
		addAttribute(ACTION_ORDER)
				.setDescription("The order in which the action is registered within its ReindexActionJob")
				.setNillable(false);
		addAttribute(ENTITY_FULL_NAME).setDescription("The full name of the entity that needs to be reindexed.")
				.setNillable(false);
		addAttribute(ENTITY_ID)
				.setDescription("Filled when only one row of the entity \"" + ENTITY_FULL_NAME + "\" is reindexed")
				.setDataType(new TextField()).setNillable(true);
		addAttribute(CUD_TYPE).setDescription(
				"Enum: The create, update and delete operation that got caused the need for the reindex")
				.setDataType(new EnumField()).setEnumOptions(CudType.class).setNillable(false);
		addAttribute(DATA_TYPE).setDescription(
				"Enum: Tells you if the data or the metadata of the \"" + ENTITY_FULL_NAME + "\" is affected")
				.setDataType(new EnumField()).setEnumOptions(DataType.class).setNillable(false);
		addAttribute(REINDEX_STATUS).setDescription("The status of reindex action").setDataType(new EnumField())
				.setEnumOptions(ReindexStatus.class).setNillable(false);
	}

	/**
	 * Indicates what type of change was made to the entity's data or metadata.
	 */
	public enum CudType
	{
		/**
		 * Entity data or metadata got added.
		 */
		ADD,
		/**
		 * Entity data or metadata got updated.
		 */
		UPDATE,
		/**
		 * Entity data or metadata got deleted.
		 */
		DELETE
	}

	/**
	 * Indicates if data or metadata got changed.
	 */
	public enum DataType
	{
		/**
		 * The data has changed.
		 */
		DATA,
		/**
		 * The entity's metadata has changed.
		 */
		METADATA
	}

	/**
	 * Reindex action status
	 */
	public enum ReindexStatus
	{
		/**
		 * reindex action is finished
		 */
		FINISHED,
		/**
		 * reindex action is canceled
		 */
		CANCELED,
		/**
		 * reindex action failed
		 */
		FAILED,
		/**
		 * reindex action is started
		 */
		STARTED,
		/**
		 * reindex action is just created and is not yet processed
		 */
		PENDING
	}
}
