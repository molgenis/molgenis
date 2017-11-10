package org.molgenis.data.decorator.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class DecoratorConfigurationFactory
		extends AbstractSystemEntityFactory<DecoratorConfiguration, DecoratorConfigurationMetadata, String>
{
	DecoratorConfigurationFactory(DecoratorConfigurationMetadata decoratorConfigurationMetadata,
			EntityPopulator entityPopulator)
	{
		super(DecoratorConfiguration.class, decoratorConfigurationMetadata, entityPopulator);
	}
}
