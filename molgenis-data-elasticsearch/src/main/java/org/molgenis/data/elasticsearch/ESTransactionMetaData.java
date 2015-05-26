package org.molgenis.data.elasticsearch;

import java.util.Arrays;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultEntityMetaData;

public class ESTransactionMetaData extends DefaultEntityMetaData
{
	public static final ESTransactionMetaData INSTANCE = new ESTransactionMetaData();
	public static final String ENTITY_NAME = "ESTransaction";
	public static final String ID = "id";
	public static final String STATUS = "status";

	public static final String STATUS_STARTED = "STARTED";
	public static final String STATUS_COMMITTED = "COMMITTED";
	public static final String STATUS_ROLLBACK = "ROLLBACK";

	private ESTransactionMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(ID).setIdAttribute(true).setNillable(false);
		addAttribute(STATUS).setDataType(MolgenisFieldTypes.ENUM)
				.setEnumOptions(Arrays.asList(STATUS_STARTED, STATUS_COMMITTED, STATUS_ROLLBACK)).setNillable(false);
	}
}
