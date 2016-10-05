package org.molgenis.genetics.diag.genenetwork.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GeneNetworkScoreFactory
		extends AbstractSystemEntityFactory<GeneNetworkScore, GeneNetworkScoreMetaData, String>
{
	@Autowired
	GeneNetworkScoreFactory(GeneNetworkScoreMetaData geneNetworkScoreMetaData, EntityPopulator entityPopulator)
	{
		super(GeneNetworkScore.class, geneNetworkScoreMetaData, entityPopulator);
	}
}
