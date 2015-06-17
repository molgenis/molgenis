package org.molgenis.data.transaction;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.fieldtypes.EnumField;
import org.springframework.stereotype.Component;

@Component
public class MolgenisTransactionLogMetaData extends DefaultEntityMetaData
{
	public static String ENTITY_NAME = "MolgenisTransactionLog";

	public static String TRANSACTION_ID = "transactionId";
	public static String USER_NAME = "userName";
	public static String START_TIME = "startTime";
	public static String STATUS = "transactionStatus";
	public static String END_TIME = "endTime";

	public static final MolgenisTransactionLogMetaData INSTANCE = new MolgenisTransactionLogMetaData();

	private MolgenisTransactionLogMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(TRANSACTION_ID).setIdAttribute(true).setNillable(false);
		addAttribute(USER_NAME).setNillable(true);
		addAttribute(START_TIME).setDataType(MolgenisFieldTypes.DATETIME).setNillable(false);
		addAttribute(STATUS).setDataType(new EnumField()).setEnumOptions(Status.getOptions()).setNillable(false);
		addAttribute(END_TIME).setDataType(MolgenisFieldTypes.DATETIME).setNillable(true);
	}

	public static enum Status
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
	};
}
