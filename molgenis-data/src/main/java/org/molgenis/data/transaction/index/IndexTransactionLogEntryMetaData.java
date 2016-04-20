package org.molgenis.data.transaction.index;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.fieldtypes.EnumField;

public class IndexTransactionLogEntryMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "index_transaction_log_entry";
	public static final String ID = "id";
	public static final String MOLGENIS_TRANSACTION_LOG = "index_transaction_log";
	public static final String LOG_ORDER = "log_order";
	public static final String ENTITY_FULL_NAME = "entity_full_name";
	public static final String ENTITY_ID = "entity_id";
	public static final String CUD_TYPE = "cud_type";
	public static final String DATA_TYPE = "data_type";

	public IndexTransactionLogEntryMetaData(IndexTransactionLogMetaData indexTransactionLogMetaData, String backend)
	{
		super(ENTITY_NAME);
		setBackend(backend);
		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false);
		addAttribute(MOLGENIS_TRANSACTION_LOG).setDataType(MolgenisFieldTypes.XREF).setRefEntity(
				indexTransactionLogMetaData);
		addAttribute(LOG_ORDER).setNillable(false);
		addAttribute(ENTITY_FULL_NAME).setNillable(false);
		addAttribute(ENTITY_ID).setNillable(true);
		addAttribute(CUD_TYPE).setDataType(new EnumField()).setEnumOptions(CudType.getOptions()).setNillable(false);
		addAttribute(DATA_TYPE).setDataType(new EnumField()).setEnumOptions(DataType.getOptions()).setNillable(false);
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
}
