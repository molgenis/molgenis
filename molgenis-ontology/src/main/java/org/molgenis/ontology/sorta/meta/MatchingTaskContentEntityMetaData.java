package org.molgenis.ontology.sorta.meta;

import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class MatchingTaskContentEntityMetaData extends EntityMetaData
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
		addAttribute(new AttributeMetaData(IDENTIFIER), ROLE_ID);
		addAttribute(
				new AttributeMetaData(MATCHED_TERM).setDescription("Matched ontology term").setNillable(true));
		addAttribute(new AttributeMetaData(SCORE, FieldTypeEnum.DECIMAL)
				.setDescription("Score of the match").setNillable(true));
		addAttribute(new AttributeMetaData(VALIDATED, FieldTypeEnum.BOOL)
				.setDescription("Indication if the match was validated").setNillable(false));
	}
}
