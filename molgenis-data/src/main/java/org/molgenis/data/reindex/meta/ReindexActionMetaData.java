package org.molgenis.data.reindex.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.reindex.meta.IndexPackage.PACKAGE_INDEX;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.fieldtypes.EnumField;
import org.molgenis.fieldtypes.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The reindex action is used to describe the action that needs to be done to make a
 * {@link org.molgenis.data.Repository}'s index consistent again.
 */
@Component
public class ReindexActionMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "ReindexAction";
	public static final String REINDEX_ACTION = PACKAGE_INDEX + PACKAGE_SEPARATOR + SIMPLE_NAME;

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

	private final IndexPackage indexPackage;
	private ReindexActionJobMetaData reindexActionJobMetaData;

	@Autowired
	public ReindexActionMetaData(IndexPackage indexPackage, ReindexActionJobMetaData reindexActionJobMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_INDEX);
		this.indexPackage = requireNonNull(indexPackage);
		this.reindexActionJobMetaData = requireNonNull(reindexActionJobMetaData);
	}

	@Override
	public void init()
	{
		setPackage(indexPackage);

		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false);
		addAttribute(REINDEX_ACTION_GROUP).setDescription("The group that this reindex action belongs to")
				.setDataType(MolgenisFieldTypes.XREF).setRefEntity(reindexActionJobMetaData);
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
		CREATE,
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