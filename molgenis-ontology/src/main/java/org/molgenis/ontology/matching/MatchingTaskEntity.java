package org.molgenis.ontology.matching;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public class MatchingTaskEntity
{
	public final static String ENTITY_NAME = "MatchingTask";
	public final static String IDENTIFIER = "Identifier";
	public final static String DATA_CREATED = "Date_created";
	public final static String MOLGENIS_USER = "Molgenis_user";
	public final static String CODE_SYSTEM = "Code_system";
	public final static String THRESHOLD = "Threshold";

	public static EntityMetaData getEntityMetaData()
	{
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData(ENTITY_NAME);
		DefaultAttributeMetaData identifierAttr = new DefaultAttributeMetaData(IDENTIFIER);
		identifierAttr.setIdAttribute(true);
		identifierAttr.setNillable(false);
		entityMetaData.addAttributeMetaData(identifierAttr);
		DefaultAttributeMetaData dataCreatedAttr = new DefaultAttributeMetaData(DATA_CREATED, FieldTypeEnum.DATE_TIME);
		dataCreatedAttr.setNillable(false);
		entityMetaData.addAttributeMetaData(dataCreatedAttr);
		DefaultAttributeMetaData molgenisUserAttr = new DefaultAttributeMetaData(MOLGENIS_USER);
		molgenisUserAttr.setNillable(false);
		entityMetaData.addAttributeMetaData(molgenisUserAttr);
		DefaultAttributeMetaData codeSystemAttr = new DefaultAttributeMetaData(CODE_SYSTEM);
		codeSystemAttr.setNillable(false);
		entityMetaData.addAttributeMetaData(codeSystemAttr);
		DefaultAttributeMetaData thresholdAttr = new DefaultAttributeMetaData(THRESHOLD, FieldTypeEnum.INT);
		thresholdAttr.setNillable(false);
		entityMetaData.addAttributeMetaData(thresholdAttr);
		return entityMetaData;
	}
}