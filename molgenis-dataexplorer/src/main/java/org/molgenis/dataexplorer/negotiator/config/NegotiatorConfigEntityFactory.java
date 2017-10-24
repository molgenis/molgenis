package org.molgenis.dataexplorer.negotiator.config;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
class NegotiatorConfigEntityFactory
		extends AbstractSystemEntityFactory<NegotiatorEntityConfig, NegotiatorEntityConfigMeta, String>
{
	NegotiatorConfigEntityFactory(NegotiatorEntityConfigMeta myEntityMeta, EntityPopulator entityPopulator)
	{
		super(NegotiatorEntityConfig.class, myEntityMeta, entityPopulator);
	}
}