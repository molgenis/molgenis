package org.molgenis.data.elasticsearch.reindex;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.fieldtypes.EnumField;
import org.molgenis.fieldtypes.TextField;

public class ReindexActionMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "reindex_action";
	public static final String ID = "id";
	public static final String REINDEX_ACTION_GROUP = "reindex_action_group";
	public static final String ACTION_ORDER = "action_order";
	public static final String ENTITY_FULL_NAME = "entity_full_name";
	public static final String ENTITY_ID = "entity_id";
	public static final String CUD_TYPE = "cud_type";
	public static final String DATA_TYPE = "data_type";
	public static final String REINDEX_STATUS = "reindex_status";

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

	public static enum CudType
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

	public static enum DataType
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
	public static enum ReindexStatus
	{
		FINISHED, CANCELED, FAILED, STARTED, NONE;

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
