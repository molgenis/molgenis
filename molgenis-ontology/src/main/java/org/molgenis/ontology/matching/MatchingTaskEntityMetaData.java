package org.molgenis.ontology.matching;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public class MatchingTaskEntityMetaData extends DefaultEntityMetaData
{
	public final static String ENTITY_NAME = "MatchingTask";
	public final static String IDENTIFIER = "Identifier";
	public final static String DATA_CREATED = "Date_created";
	public final static String MOLGENIS_USER = "Molgenis_user";
	public final static String CODE_SYSTEM = "Code_system";
	public final static String THRESHOLD = "Threshold";
	public static final MatchingTaskEntityMetaData INSTANCE = new MatchingTaskEntityMetaData();

	private MatchingTaskEntityMetaData()
	{
		super(ENTITY_NAME);
		addAttributeMetaData(new DefaultAttributeMetaData(IDENTIFIER), ROLE_ID);
		addAttributeMetaData(new DefaultAttributeMetaData(DATA_CREATED, FieldTypeEnum.DATE_TIME).setNillable(false));
		addAttributeMetaData(new DefaultAttributeMetaData(MOLGENIS_USER).setNillable(false));
		addAttributeMetaData(new DefaultAttributeMetaData(CODE_SYSTEM).setNillable(false));
		addAttributeMetaData(new DefaultAttributeMetaData(THRESHOLD, FieldTypeEnum.INT).setNillable(false));
	}
}