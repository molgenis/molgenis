package org.molgenis.ontology.sorta.meta;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;

public class OntologyTermHitEntityMetaData extends DefaultEntityMetaData
{
	public static final String SCORE = "Score";
	public static final String COMBINED_SCORE = "Combined_Score";
	public static final DefaultEntityMetaData INSTANCE = new OntologyTermHitEntityMetaData();

	public OntologyTermHitEntityMetaData()
	{
		super(OntologyTermMetaData.INSTANCE);
		addAttribute(SCORE).setDataType(MolgenisFieldTypes.DECIMAL);
		addAttribute(COMBINED_SCORE).setDataType(MolgenisFieldTypes.DECIMAL);
	}
}
