package org.molgenis.data.transaction;

import org.molgenis.data.support.DefaultEntityMetaData;

//@Component
public class TransactionMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "MolgenisTransaction";
	public static final String ATTR_ID = "id";
	public static final String ATTR_TRANSACTION_ID = "transactionId";
	public static final String ATTR_ENTITY_NAME = "entityName";
	public static final String ATTR_ENTITY_ID = "entityId";

	public TransactionMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(ATTR_ID).setAuto(true).setIdAttribute(true).setNillable(false).setUnique(true);
		addAttribute(ATTR_TRANSACTION_ID);
		addAttribute(ATTR_ENTITY_ID);
	}
}
