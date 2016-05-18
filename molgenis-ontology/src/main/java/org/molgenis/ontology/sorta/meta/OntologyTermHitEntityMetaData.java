package org.molgenis.ontology.sorta.meta;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.springframework.stereotype.Component;

@Component
public class OntologyTermHitEntityMetaData extends SystemEntityMetaDataImpl
{
	public static final String ENTITY_NAME = "OntologyTermHit";
	public static final String SCORE = "Score";
	public static final String COMBINED_SCORE = "Combined_Score";

	@Override
	public void init()
	{
		setName(ENTITY_NAME);
		addAttribute(SCORE).setDataType(MolgenisFieldTypes.DECIMAL);
		addAttribute(COMBINED_SCORE).setDataType(MolgenisFieldTypes.DECIMAL);
	}
}
