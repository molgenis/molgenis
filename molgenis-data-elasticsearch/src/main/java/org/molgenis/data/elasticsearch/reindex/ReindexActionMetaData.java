package org.molgenis.data.elasticsearch.reindex;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.elasticsearch.reindex.IndexPackage.PACKAGE_INDEX;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.meta.RootSystemPackage.PACKAGE_SYSTEM;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.molgenis.fieldtypes.EnumField;
import org.molgenis.fieldtypes.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The reindex action is used to describe the action that need to be done to get the index consistent again.
 */
@Component
public class ReindexActionMetaData extends SystemEntityMetaDataImpl
{
	public static final String SIMPLE_NAME = "ReindexAction";
	public static final String REINDEX_ACTION = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

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
	 * <p>
	 * FINISHED: reindex action is finished. CANCELED: reindex action is canceled FAILED: reindex action failed STARTED:
	 * reindex action is started pending: reindex action is just created and is not proced
	 */
	public static final String REINDEX_STATUS = "reindexStatus";

	private final IndexPackage indexPackage;
	private final ReindexActionJobMetaData indexTransactionLogMetaData;

	@Autowired
	public ReindexActionMetaData(IndexPackage indexPackage, ReindexActionJobMetaData indexTransactionLogMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_INDEX);

		this.indexPackage = requireNonNull(indexPackage);
		this.indexTransactionLogMetaData = requireNonNull(indexTransactionLogMetaData);
	}

	@Override
	public void init()
	{
		setPackage(indexPackage);

		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false);
		addAttribute(REINDEX_ACTION_GROUP).setDataType(MolgenisFieldTypes.XREF)
				.setRefEntity(indexTransactionLogMetaData);
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
	}

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
	}

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
	}
}
