package org.molgenis.data.transaction;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultEntityMetaData;

public class LockMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "MolgenisLock";
	public static final String ID = "id";
	public static final String MOLGENIS_TRANSACTION_LOG = "molgenisTransactionLog";
	public static final String ENTITY = "entity";
	public static final String ADD_LOCKED = "addLocked";
	public static final String UPDATE_LOCKED = "updateLocked";
	public static final String DELETE_LOCKED = "deleteLocked";

	LockMetaData(MolgenisTransactionLogMetaData molgenisTransactionLogMetaData, String backend)
	{
		super(ENTITY_NAME);
		setBackend(backend);
		addAttribute(ID).setIdAttribute(true).setAuto(true).setNillable(false);
		addAttribute(MOLGENIS_TRANSACTION_LOG).setDataType(MolgenisFieldTypes.XREF)
				.setRefEntity(molgenisTransactionLogMetaData).setNillable(false);
		addAttribute(ENTITY).setNillable(false);
		addAttribute(ADD_LOCKED).setDataType(MolgenisFieldTypes.BOOL).setNillable(false);
		addAttribute(UPDATE_LOCKED).setDataType(MolgenisFieldTypes.BOOL).setNillable(false);
		addAttribute(DELETE_LOCKED).setDataType(MolgenisFieldTypes.BOOL).setNillable(false);
	}

}
