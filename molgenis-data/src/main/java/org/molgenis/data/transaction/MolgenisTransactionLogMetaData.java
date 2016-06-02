package org.molgenis.data.transaction;

import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.molgenis.fieldtypes.EnumField;

public class MolgenisTransactionLogMetaData extends SystemEntityMetaDataImpl
{
	public static final String MOLGENIS_TRANSACTION_LOG = "MolgenisTransactionLog";

	public static final String TRANSACTION_ID = "transactionId";
	public static final String USER_NAME = "userName";
	public static final String START_TIME = "startTime";
	public static final String STATUS = "transactionStatus";
	public static final String END_TIME = "endTime";

	private final String transactionLogBackend;

	MolgenisTransactionLogMetaData(String transactionLogBackend)
	{
		super(MOLGENIS_TRANSACTION_LOG);
		this.transactionLogBackend = transactionLogBackend;
	}

	public void init() {
		setBackend(transactionLogBackend);
		addAttribute(TRANSACTION_ID, ROLE_ID);
		addAttribute(USER_NAME).setNillable(true);
		addAttribute(START_TIME).setDataType(MolgenisFieldTypes.DATETIME).setNillable(false);
		addAttribute(STATUS).setDataType(new EnumField()).setEnumOptions(Status.getOptions()).setNillable(false);
		addAttribute(END_TIME).setDataType(MolgenisFieldTypes.DATETIME).setNillable(true);
	}

	public enum Status
	{
		STARTED, COMMITED, ROLLBACK;

		private static List<String> getOptions()
		{
			List<String> options = new ArrayList<String>();
			for (Status status : Status.values())
			{
				options.add(status.name());
			}

			return options;
		}
	}
}
