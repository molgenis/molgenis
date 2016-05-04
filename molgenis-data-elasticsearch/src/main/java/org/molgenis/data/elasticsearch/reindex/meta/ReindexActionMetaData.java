package org.molgenis.data.elasticsearch.reindex.meta;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexActionJobMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.fieldtypes.EnumField;
import org.molgenis.fieldtypes.TextField;

/**
 * The reindex action is used to describe the action that need to be done to get the index consistent again.
 */
public class ReindexActionMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "ReindexAction";

	/**
	 * Is auto generated
	 */
	public static final String ID = "id";

	/**
	 * A reference to the group where it belongs
	 */
	public static final String REINDEX_ACTION_GROUP = "reindexActionGroup";

	/**
	 * The order in which the action is registered
	 */
	public static final String ACTION_ORDER = "actionOrder";

	/**
	 * The entity full name
	 */
	public static final String ENTITY_FULL_NAME = "entityFullName";

	/**
	 * Entity is filled when only one row of the entity "entityFullName" is effected
	 */
	public static final String ENTITY_ID = "entityId";

	/**
	 * Enum: The create, update and delete operations
	 */
	public static final String CUD_TYPE = "cudType";

	/**
	 * Enum: Tells you if the data or the metadata of the "entity_full_name" is effected
	 */
	public static final String DATA_TYPE = "dataType";

	/**
	 * Enum: the status of reindex action
	 * 
	 * FINISHED: reindex action is finished. CANCELED: reindex action is canceled FAILED: reindex action failed STARTED:
	 * reindex action is started pending: reindex action is just created and is not proced
	 */
	public static final String REINDEX_STATUS = "reindexStatus";

	public ReindexActionMetaData(ReindexActionJobMetaData indexTransactionLogMetaData, String backend)
	{
		super(ENTITY_NAME);
		setBackend(backend);
		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false);
		addAttribute(REINDEX_ACTION_GROUP).setDataType(MolgenisFieldTypes.XREF).setRefEntity(
				indexTransactionLogMetaData);
		addAttribute(ACTION_ORDER).setNillable(false);
		addAttribute(ENTITY_FULL_NAME).setNillable(false);
		addAttribute(ENTITY_ID).setDataType(new TextField()).setNillable(true);
		addAttribute(CUD_TYPE).setDataType(new EnumField()).setEnumOptions(CudType.getOptions()).setNillable(false);
		addAttribute(DATA_TYPE).setDataType(new EnumField()).setEnumOptions(DataType.getOptions()).setNillable(false);
		addAttribute(REINDEX_STATUS).setDataType(new EnumField()).setEnumOptions(ReindexStatus.getOptions())
				.setNillable(false);
	}

	public enum CudType
	{
		ADD, UPDATE, DELETE;

		private static List<String> getOptions()
		{
			List<String> options = new ArrayList<String>();
			for (CudType type : CudType.values())
			{
				options.add(type.name());
			}

			return options;
		}
	};

	public enum DataType
	{
		DATA, METADATA;

		private static List<String> getOptions()
		{
			List<String> options = new ArrayList<String>();
			for (DataType type : DataType.values())
			{
				options.add(type.name());
			}

			return options;
		}
	};

	/**
	 * Indexing transaction status
	 */
	public enum ReindexStatus
	{
		FINISHED, CANCELED, FAILED, STARTED, PENDING;

		private static List<String> getOptions()
		{
			List<String> options = new ArrayList<String>();
			for (ReindexStatus status : ReindexStatus.values())
			{
				options.add(status.name());
			}

			return options;
		}
	};
}
