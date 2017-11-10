package org.molgenis.data.decorator.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class DynamicDecoratorFactory
		extends AbstractSystemEntityFactory<DynamicDecorator, DynamicDecoratorMetadata, String>
{
	DynamicDecoratorFactory(DynamicDecoratorMetadata dynamicDecoratorMetadata, EntityPopulator entityPopulator)
	{
		super(DynamicDecorator.class, dynamicDecoratorMetadata, entityPopulator);
	}
}
