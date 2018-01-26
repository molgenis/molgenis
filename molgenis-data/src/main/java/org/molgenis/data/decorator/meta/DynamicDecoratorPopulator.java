package org.molgenis.data.decorator.meta;

import org.molgenis.data.DataService;
import org.molgenis.data.decorator.DynamicRepositoryDecoratorFactory;
import org.molgenis.data.decorator.DynamicRepositoryDecoratorRegistry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.DECORATOR_CONFIGURATION;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.DYNAMIC_DECORATORS;
import static org.molgenis.data.decorator.meta.DynamicDecoratorMetadata.DYNAMIC_DECORATOR;

@Component
public class DynamicDecoratorPopulator
{
	private final DataService dataService;
	private final DynamicRepositoryDecoratorRegistry registry;
	private final DynamicDecoratorFactory dynamicDecoratorFactory;

	public DynamicDecoratorPopulator(DataService dataService,
			DynamicRepositoryDecoratorRegistry dynamicRepositoryDecoratorRegistry,
			DynamicDecoratorFactory dynamicDecoratorFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.registry = requireNonNull(dynamicRepositoryDecoratorRegistry);
		this.dynamicDecoratorFactory = requireNonNull(dynamicDecoratorFactory);
	}

	public void populate()
	{
		addNewDecorators();
		removeNonExistingDecorators();
	}

	private void addNewDecorators()
	{
		dataService.add(DYNAMIC_DECORATOR,
				registry.getFactoryIds().filter(this::notPersisted).map(this::createDecorator));
	}

	private void removeNonExistingDecorators()
	{
		Set<String> nonExistingDecorators = getNonExistingDecorators();
		updateDecoratorConfigurations(nonExistingDecorators);
		dataService.deleteAll(DYNAMIC_DECORATOR, (Stream<Object>) (Stream<?>) nonExistingDecorators.stream());
	}

	private Set<String> getNonExistingDecorators()
	{
		Set<String> decorators = dataService.findAll(DYNAMIC_DECORATOR, DynamicDecorator.class)
											.map(DynamicDecorator::getId)
											.collect(toSet());

		decorators.removeAll(registry.getFactoryIds().collect(toSet()));
		return decorators;
	}

	private void updateDecoratorConfigurations(Set<String> nonExistingDecorators)
	{
		Stream<DecoratorConfiguration> updatedEntities = dataService.findAll(DECORATOR_CONFIGURATION,
				DecoratorConfiguration.class).map(config -> removeReferences(nonExistingDecorators, config));
		dataService.update(DECORATOR_CONFIGURATION, updatedEntities);
	}

	private DecoratorConfiguration removeReferences(Set<String> nonExistingDecorators,
			DecoratorConfiguration configuration)
	{
		List<DynamicDecorator> decorators = StreamSupport.stream(
				configuration.getEntities(DYNAMIC_DECORATORS, DynamicDecorator.class).spliterator(), false)
														 .filter(e -> !nonExistingDecorators.contains(e.getId()))
														 .collect(toList());

		configuration.set(DYNAMIC_DECORATORS, decorators);
		return configuration;
	}

	private boolean notPersisted(String id)
	{
		return dataService.findOneById(DYNAMIC_DECORATOR, id, DynamicDecorator.class) == null;
	}

	private DynamicDecorator createDecorator(String id)
	{
		DynamicRepositoryDecoratorFactory factory = registry.getFactory(id);
		DynamicDecorator dynamicDecorator = dynamicDecoratorFactory.create(id)
																   .setLabel(factory.getLabel())
																   .setDescription(factory.getDescription());
		return dynamicDecorator;
	}
}