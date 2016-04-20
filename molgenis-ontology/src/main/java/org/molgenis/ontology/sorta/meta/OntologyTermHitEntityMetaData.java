package org.molgenis.ontology.sorta.meta;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;

public class OntologyTermHitEntityMetaData extends EntityMetaData
{
	public static final String SCORE = "Score";
	public static final String COMBINED_SCORE = "Combined_Score";
	public static final EntityMetaData INSTANCE = new OntologyTermHitEntityMetaData();

	public OntologyTermHitEntityMetaData()
	{
		super(OntologyTermMetaData.INSTANCE);
		addAttribute(SCORE).setDataType(MolgenisFieldTypes.DECIMAL);
		addAttribute(COMBINED_SCORE).setDataType(MolgenisFieldTypes.DECIMAL);
	}
}
