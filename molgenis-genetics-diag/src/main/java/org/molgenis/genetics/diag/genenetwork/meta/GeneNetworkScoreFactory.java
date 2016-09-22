package org.molgenis.genetics.diag.genenetwork.meta;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.MolgenisUserMetaData;
import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GeneNetworkScoreFactory extends AbstractSystemEntityFactory<GeneNetworkScore, GeneNetworkScoreMetaData, String>
{
	@Autowired
	GeneNetworkScoreFactory(GeneNetworkScoreMetaData geneNetworkScoreMetaData)
	{
		super(GeneNetworkScore.class, geneNetworkScoreMetaData);
	}
}
