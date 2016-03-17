package org.molgenis.ontology.matching;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public class MatchingTaskContentEntityMetaData extends DefaultEntityMetaData
{
	public final static String ENTITY_NAME = "MatchingTaskContent";
	public final static String IDENTIFIER = "Identifier";
	public final static String INPUT_TERM = "Input_term";
	public final static String REF_ENTITY = "Ref_entity";
	public final static String MATCHED_TERM = "Match_term";
	public final static String SCORE = "Score";
	public final static String VALIDATED = "Validated";

	public static final MatchingTaskContentEntityMetaData INSTANCE = new MatchingTaskContentEntityMetaData();

	private MatchingTaskContentEntityMetaData()
	{
		super(ENTITY_NAME);
		addAttributeMetaData(new DefaultAttributeMetaData(IDENTIFIER), ROLE_ID);
		addAttributeMetaData(new DefaultAttributeMetaData(INPUT_TERM).setNillable(false));
		addAttributeMetaData(new DefaultAttributeMetaData(REF_ENTITY).setNillable(false));
		addAttributeMetaData(new DefaultAttributeMetaData(MATCHED_TERM).setNillable(true));
		addAttributeMetaData(new DefaultAttributeMetaData(SCORE, FieldTypeEnum.DECIMAL).setNillable(true));
		addAttributeMetaData(new DefaultAttributeMetaData(VALIDATED, FieldTypeEnum.BOOL).setNillable(false));
	}
}
