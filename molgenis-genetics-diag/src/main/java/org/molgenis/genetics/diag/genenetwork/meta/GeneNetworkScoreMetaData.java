package org.molgenis.genetics.diag.genenetwork.meta;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import static org.molgenis.AttributeType.DECIMAL;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.*;

@Component
public class GeneNetworkScoreMetaData extends SystemEntityType
{
	public static final String SIMPLE_NAME = "GeneNetworkScore";

	public static final String ENSEMBL_ID = "ensemblId";
	public static final String HPO = "hpo";
	public static final String SCORE = "score";
	public static final String ID = "id";
	public static final String HUGO_SYMBOL = "hugo";

	public GeneNetworkScoreMetaData()
	{
		super(SIMPLE_NAME);
	}

	@Override
	public void init()
	{
		setLabel("Gene Network Score");

		setDescription("The gene network score for a combination of a gene and a hpo term");

		addAttribute(ID, ROLE_ID).setUnique(true).setAuto(true).setVisible(false).setDescription("TODO");
		addAttribute(HPO, ROLE_LABEL, ROLE_LOOKUP).setLabel("HPO ID").setDescription("").setNillable(false)
				.setAggregatable(true);
		addAttribute(ENSEMBL_ID).setLabel("Gene").setDescription("The Ensembl identifier").setNillable(false)
				.setAggregatable(true);
		addAttribute(HUGO_SYMBOL).setLabel("HUGO Symbol").setDescription("The HGNC gene symbol").setAggregatable(true);
		addAttribute(SCORE).setLabel("Gene Network Score").setDataType(DECIMAL).setAggregatable(true)
				.setNillable(false);
	}
}
