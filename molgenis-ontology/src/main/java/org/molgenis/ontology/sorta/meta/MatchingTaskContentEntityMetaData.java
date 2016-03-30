package org.molgenis.ontology.sorta.meta;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class MatchingTaskContentEntityMetaData extends DefaultEntityMetaData
{
	public final static String ENTITY_NAME = "MatchingTaskContent";
	public final static String IDENTIFIER = "identifier";
	public final static String INPUT_TERM = "inputTerm";
	public final static String MATCHED_TERM = "matchTerm";
	public final static String SCORE = "score";
	public final static String VALIDATED = "validated";

	public static final MatchingTaskContentEntityMetaData INSTANCE = new MatchingTaskContentEntityMetaData();

	private MatchingTaskContentEntityMetaData()
	{
		super(ENTITY_NAME);
		setAbstract(true);
		addAttributeMetaData(new DefaultAttributeMetaData(IDENTIFIER), ROLE_ID);
		addAttributeMetaData(
				new DefaultAttributeMetaData(MATCHED_TERM).setDescription("Matched ontology term").setNillable(true));
		addAttributeMetaData(new DefaultAttributeMetaData(SCORE, FieldTypeEnum.DECIMAL)
				.setDescription("Score of the match").setNillable(true));
		addAttributeMetaData(new DefaultAttributeMetaData(VALIDATED, FieldTypeEnum.BOOL)
				.setDescription("Indication if the match was validated").setNillable(false));
	}
}
