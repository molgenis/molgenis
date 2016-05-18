package org.molgenis.ontology.sorta.meta;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.DECIMAL;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.stereotype.Component;

@Component
public class MatchingTaskContentEntityMetaData extends SystemEntityMetaDataImpl
{
	public final static String ENTITY_NAME = "MatchingTaskContent";
	public final static String IDENTIFIER = "identifier";
	public final static String INPUT_TERM = "inputTerm";
	public final static String MATCHED_TERM = "matchTerm";
	public final static String SCORE = "score";
	public final static String VALIDATED = "validated";

	@Override
	public void init()
	{
		setName(ENTITY_NAME);
		setAbstract(true);
		addAttribute(IDENTIFIER, ROLE_ID);
		addAttribute(MATCHED_TERM).setDescription("Matched ontology term").setNillable(true);
		addAttribute(SCORE).setDataType(DECIMAL)
				.setDescription("Score of the match").setNillable(true);
		addAttribute(VALIDATED).setDataType(BOOL)
				.setDescription("Indication if the match was validated").setNillable(false);
	}
}
