package org.molgenis.data.decorator.meta;

import org.molgenis.data.DataService;
import org.molgenis.data.decorator.DynamicRepositoryDecoratorRegistry;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.decorator.meta.DynamicDecoratorMetadata.DYNAMIC_DECORATOR;

@Component
public class DynamicDecoratorPopulator
{
	private final DataService dataService;
	private final DynamicRepositoryDecoratorRegistry dynamicRepositoryDecoratorRegistry;
	private final DynamicDecoratorFactory dynamicDecoratorFactory;

	public DynamicDecoratorPopulator(DataService dataService,
			DynamicRepositoryDecoratorRegistry dynamicRepositoryDecoratorRegistry,
			DynamicDecoratorFactory dynamicDecoratorFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.dynamicRepositoryDecoratorRegistry = requireNonNull(dynamicRepositoryDecoratorRegistry);
		this.dynamicDecoratorFactory = requireNonNull(dynamicDecoratorFactory);
	}

	public void populate()
	{
		dataService.add(DYNAMIC_DECORATOR, dynamicRepositoryDecoratorRegistry.getFactoryIds()
																			 .filter(this::notExists)
																			 .map(this::createDynamicDecorator));
	}

	private boolean notExists(String id)
	{
		return dataService.findOneById(DYNAMIC_DECORATOR, id, DynamicDecorator.class) == null;
	}

	private DynamicDecorator createDynamicDecorator(String id)
	{
		return dynamicDecoratorFactory.create(id);
	}
}

