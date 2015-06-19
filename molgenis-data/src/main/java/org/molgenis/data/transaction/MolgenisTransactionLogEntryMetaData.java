package org.molgenis.data.transaction;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.fieldtypes.EnumField;
import org.springframework.stereotype.Component;

@Component
public class MolgenisTransactionLogEntryMetaData extends DefaultEntityMetaData
{
	public static String ENTITY_NAME = "MolgenisTransactionLogEntry";

	public static String ID = "id";
	public static String MOLGENIS_TRANSACTION_LOG = "molgenisTransactionLog";
	public static String ENTITY = "entity";
	public static String TYPE = "type";

	public static final MolgenisTransactionLogEntryMetaData INSTANCE = new MolgenisTransactionLogEntryMetaData();

	private MolgenisTransactionLogEntryMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(ID).setIdAttribute(true).setAuto(true).setNillable(false).setVisible(false);
		addAttribute(MOLGENIS_TRANSACTION_LOG).setDataType(MolgenisFieldTypes.XREF).setRefEntity(
				MolgenisTransactionLogMetaData.INSTANCE);
		addAttribute(ENTITY).setNillable(false);
		addAttribute(TYPE).setDataType(new EnumField()).setEnumOptions(Type.getOptions()).setNillable(false);
	}

	public static enum Type
	{
		ADD, UPDATE, DELETE;

		private static List<String> getOptions()
		{
			List<String> options = new ArrayList<String>();
			for (Type type : Type.values())
			{
				options.add(type.name());
			}

			return options;
		}
	};
}
