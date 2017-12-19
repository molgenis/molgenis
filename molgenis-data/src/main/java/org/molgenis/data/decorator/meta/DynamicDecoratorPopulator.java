package org.molgenis.data.decorator.meta;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
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
		Set<Object> nonExistingDecorators = getNonExistingDecorators();
		updateDecoratorConfigurations(nonExistingDecorators);
		dataService.deleteAll(DYNAMIC_DECORATOR, nonExistingDecorators.stream());
	}

	private Set<Object> getNonExistingDecorators()
	{
		Set<Object> decorators = dataService.findAll(DYNAMIC_DECORATOR).map(Entity::getIdValue).collect(toSet());
		decorators.removeAll(registry.getFactoryIds().collect(toSet()));
		return decorators;
	}

	private void updateDecoratorConfigurations(Set<Object> nonExistingDecorators)
	{
		Stream<Entity> updatedEntities = dataService.findAll(DECORATOR_CONFIGURATION)
													.map(entity -> removeReferences(nonExistingDecorators, entity));
		dataService.update(DECORATOR_CONFIGURATION, updatedEntities);
	}

	private Entity removeReferences(Set<Object> nonExistingDecorators, Entity entity)
	{
		List<Entity> decorators = StreamSupport.stream(entity.getEntities(DYNAMIC_DECORATORS).spliterator(), false)
											   .filter(e -> !nonExistingDecorators.contains(e.getIdValue()))
											   .collect(toList());
		entity.set(DYNAMIC_DECORATORS, decorators);
		return entity;
	}

	private boolean notPersisted(String id)
	{
		return dataService.findOneById(DYNAMIC_DECORATOR, id, DynamicDecorator.class) == null;
	}

	private DynamicDecorator createDecorator(String id)
	{
		return dynamicDecoratorFactory.create(id);
	}
}

