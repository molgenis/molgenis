package org.molgenis.data.decorator;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Objects.requireNonNull;

@Component
public class DynamicRepositoryDecoratorFactoryRegistrar
{
	private final DynamicRepositoryDecoratorRegistry repositoryDecoratorRegistry;

	public DynamicRepositoryDecoratorFactoryRegistrar(DynamicRepositoryDecoratorRegistry repositoryDecoratorRegistry)
	{
		this.repositoryDecoratorRegistry = requireNonNull(repositoryDecoratorRegistry);
	}

	public void register(ApplicationContext context)
	{
		Map<String, DynamicRepositoryDecoratorFactory> repositoryDecoratorFactoryMap = context.getBeansOfType(
				DynamicRepositoryDecoratorFactory.class);
		repositoryDecoratorFactoryMap.values().forEach(repositoryDecoratorRegistry::addFactory);
	}
}