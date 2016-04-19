package org.molgenis.data.transaction.index;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.fieldtypes.EnumField;

public class IndexTransactionLogMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "index_transaction_log";
	public static final String TRANSACTION_ID = "transaction_id";
	public static final String USER_NAME = "user_name";
	public static final String START_TIME = "start_time";
	public static final String TRANSACTION_STATUS = "transaction_status";
	public static final String INDEX_STATUS = "index_status";
	public static final String END_TIME = "end_time";
	public static final String LOG_COUNT = "log_count";

	public IndexTransactionLogMetaData(String backend)
	{
		super(ENTITY_NAME);
		setBackend(backend);
		addAttribute(TRANSACTION_ID, ROLE_ID);
		addAttribute(USER_NAME).setNillable(true);
		addAttribute(START_TIME).setDataType(MolgenisFieldTypes.DATETIME).setNillable(false);
		addAttribute(TRANSACTION_STATUS).setDataType(new EnumField()).setEnumOptions(TransactionStatus.getOptions())
				.setNillable(false);
		addAttribute(INDEX_STATUS).setDataType(new EnumField()).setEnumOptions(IndexStatus.getOptions())
				.setNillable(false);
		addAttribute(END_TIME).setDataType(MolgenisFieldTypes.DATETIME).setNillable(true);
		addAttribute(LOG_COUNT).setDataType(MolgenisFieldTypes.INT).setNillable(false);
	}

	/**
	 * Transaction status
	 * 
	 * @author jonathanjetten
	 *
	 */
	public static enum TransactionStatus
	{
		STARTED, COMMITED, ROLLBACK;

		private static List<String> getOptions()
		{
			List<String> options = new ArrayList<String>();
			for (TransactionStatus status : TransactionStatus.values())
			{
				options.add(status.name());
			}

			return options;
		}
	};

	/**
	 * Indexing transaction status
	 * 
	 * @author jonathanjetten
	 *
	 */
	public static enum IndexStatus
	{
		FINISHED, CANCELED, FAILED, STARTED, NONE;

		private static List<String> getOptions()
		{
			List<String> options = new ArrayList<String>();
			for (IndexStatus status : IndexStatus.values())
			{
				options.add(status.name());
			}

			return options;
		}
	};
}
